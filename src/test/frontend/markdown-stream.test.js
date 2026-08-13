#!/usr/bin/env node
/**
 * 客服流式 Markdown 渲染 —— 回归测试
 *
 * 运行: node src/test/frontend/markdown-stream.test.js
 *       node src/test/frontend/markdown-stream.test.js --perf   (附带性能测量)
 *
 * 前端无构建系统, 全部内嵌在 static/index.html 里, 因此测试直接从 <script> 抽取
 * 纯函数放进 vm 沙箱验证; 涉及 DOM 的增量渲染器则用下方极简 stub 驱动。
 *
 * 覆盖三件事:
 *   1. 语法 —— 内嵌 script 能否解析
 *   2. 渲染正确性 —— 行内/块级语法、乐观闭合、误判防护、工具标记
 *   3. 增量渲染不变式 —— 已提交的块永不被改写, 且流式最终结果 === 整段渲染结果
 */
const fs = require('fs');
const path = require('path');
const vm = require('vm');

const PAGE = path.join(__dirname, '..', '..', 'main', 'resources', 'static', 'index.html');

// ── 从 index.html 抽取被测代码 ──────────────────────────────────
const scripts = [...fs.readFileSync(PAGE, 'utf8')
  .matchAll(/<script\b[^>]*>([\s\S]*?)<\/script>/g)].map(m => m[1]);
const src = scripts.join('\n');

/** 按花括号配对截取一个函数声明(本文件内正则的 {} 均成对, 故计数可靠) */
function grabFn(name) {
  const start = src.indexOf('function ' + name + '(');
  if (start < 0) throw new Error('函数未找到: ' + name);
  let depth = 0;
  for (let i = src.indexOf('{', start); i < src.length; i++) {
    if (src[i] === '{') depth++;
    else if (src[i] === '}' && --depth === 0) return src.slice(start, i + 1);
  }
  throw new Error('花括号不配对: ' + name);
}

// ── 极简 DOM stub ───────────────────────────────────────────────
// 只实现渲染器用到的语义。关键点: insertBefore 必须把节点从原父节点摘除
// (真实 DOM 的 reparent 行为) —— finish() 里的 while(tail.firstChild) 依赖它才能终止。
class El {
  constructor(tag) {
    this.tag = tag;
    this.children = [];
    this.parent = null;
    this._html = '';
    this.className = '';
    if (tag === 'template') this.content = { firstElementChild: null };
  }
  appendChild(node) { node.parent = this; this.children.push(node); return node; }
  insertBefore(node, ref) {
    if (node.parent) node.parent.removeChild(node);
    const at = this.children.indexOf(ref);
    this.children.splice(at < 0 ? this.children.length : at, 0, node);
    node.parent = this;
    return node;
  }
  removeChild(node) {
    const at = this.children.indexOf(node);
    if (at >= 0) this.children.splice(at, 1);
    node.parent = null;
  }
  remove() { if (this.parent) this.parent.removeChild(this); }
  get firstChild() { return this.children[0] || null; }
  set innerHTML(html) {
    this._html = html;
    const node = html ? Object.assign(new El('#text'), { _html: html }) : null;
    if (this.tag === 'template') { this.content.firstElementChild = node; return; }
    this.children.forEach(c => { c.parent = null; });
    this.children = node ? [node] : [];
    if (node) node.parent = this;
  }
  get innerHTML() { return this._html; }
  querySelector() { return null; }
  querySelectorAll() { return []; }
}

let rafQueue = [];
const sandbox = {
  console,
  document: { createElement: tag => new El(tag) },
  requestAnimationFrame: fn => rafQueue.push(fn),
  cancelAnimationFrame: () => { rafQueue = []; },
};
vm.createContext(sandbox);

/** 反复执行排队的 rAF 回调直到静止(渲染器会自我调度直至缓冲排空) */
function flushFrames() {
  let rounds = 0;
  while (rafQueue.length) {
    if (++rounds > 5000) throw new Error('rAF 未收敛: tick 没有推进缓冲队列');
    const batch = rafQueue;
    rafQueue = [];
    batch.forEach(fn => fn());
  }
  return rounds;
}

// ── 断言 ────────────────────────────────────────────────────────
let passed = 0;
const failures = [];
function check(name, actual, expected) {
  if (actual === expected) { passed++; return; }
  failures.push(`${name}\n     实际: ${JSON.stringify(actual)}\n     预期: ${JSON.stringify(expected)}`);
}
function section(title) { console.log('\n' + title); }

// ── 1) 语法 ─────────────────────────────────────────────────────
section('语法检查');
scripts.forEach((code, i) => {
  try {
    new vm.Script(code);
    console.log(`  script#${i}  OK  (${code.split('\n').length} 行)`);
    passed++;
  } catch (e) {
    failures.push(`script#${i} 语法错误: ${e.message}`);
  }
});

// ── 载入被测函数 ────────────────────────────────────────────────
vm.runInContext([
  src.match(/const TOOL_ICON = '[^']*';/)[0],
  src.match(/const TOOL_MARK_RE = [^\n]*/)[0],
  ...['escHtml', 'toolChipHtml', 'renderInline', 'parseBlocks', 'renderMarkdown',
    'closeOpenMarks', 'alignMark', 'scrollFollow', 'addCodeCopyButtons',
    'createStreamRenderer'].map(grabFn),
].join('\n'), sandbox);

const { renderMarkdown, closeOpenMarks, alignMark, parseBlocks, createStreamRenderer } = sandbox;
const blockCount = t => parseBlocks(t).length;

// ── 2) 渲染正确性 ───────────────────────────────────────────────
section('行内语法与误判防护');
check('行内代码不被斜体污染', renderMarkdown('`select *b* from`'), '<p><code>select *b* from</code></p>');
check('乘法式不被误斜体', renderMarkdown('3 * 4 * 5'), '<p>3 * 4 * 5</p>');
check('SQL 通配符不被误斜体', renderMarkdown('select * from product'), '<p>select * from product</p>');
check('正常斜体仍生效', renderMarkdown('这是 *斜体* 文字'), '<p>这是 <em>斜体</em> 文字</p>');
check('正常粗体仍生效', renderMarkdown('这是 **粗体** 文字'), '<p>这是 <strong>粗体</strong> 文字</p>');
check('金额不被误判成列表', renderMarkdown('3,998.00'), '<p>3,998.00</p>');
check('HTML 被转义', renderMarkdown('<script>x</script>'), '<p>&lt;script&gt;x&lt;/script&gt;</p>');
check('占位符形态的正文不被误还原', renderMarkdown('数字 <0> 与 <12>'), '<p>数字 &lt;0&gt; 与 &lt;12&gt;</p>');

section('乐观闭合(流式尾部未配对标记)');
check('半截粗体补全', closeOpenMarks('我们**推'), '我们**推**');
check('半截斜体补全', closeOpenMarks('我们*斜'), '我们*斜*');
check('半截行内代码补全', closeOpenMarks('值 `abc'), '值 `abc`');
check('SQL 通配符不补', closeOpenMarks('select * from t'), 'select * from t');
check('尾部裸星号不补', closeOpenMarks('select *'), 'select *');
check('乘法式不补', closeOpenMarks('3 * 4 * 5'), '3 * 4 * 5');
check('已配对不重复补', closeOpenMarks('**粗体**完'), '**粗体**完');
check('配对后再开新口', closeOpenMarks('**粗体**又**新'), '**粗体**又**新**');
check('代码围栏内不补', closeOpenMarks('```js\nlet a = b * c'), '```js\nlet a = b * c');
// 零跳变的核心保证: 半截与闭合后渲染成同一形态
check('半截即成粗体', renderMarkdown(closeOpenMarks('我们**推')), '<p>我们<strong>推</strong></p>');
check('闭合后形态一致', renderMarkdown('我们**推荐**'), '<p>我们<strong>推荐</strong></p>');

section('块级封口(增量渲染的正确性前提)');
check('单段落 = 1 块', blockCount('第一段'), 1);
check('段落+空行+段落 = 2 块', blockCount('第一段\n\n第二段'), 2);
check('标题后接正文 = 2 块', blockCount('## 标题\n正文'), 2);
check('表格首行即成表', renderMarkdown('| 商品 | 库存 |'),
  '<div class="table-scroll"><table><tr><th>商品</th><th>库存</th></tr></table></div>');
check('表格加分隔行仍 1 块', blockCount('| 商品 | 库存 |\n|---|---|'), 1);
check('表格三行仍 1 块', blockCount('| 商品 | 库存 |\n|---|---|\n| A | 5 |'), 1);
check('未闭合围栏即成代码块', renderMarkdown('```js'), '<pre><code></code></pre>');
check('围栏含内容', renderMarkdown('```js\nlet a=1'), '<pre><code>let a=1</code></pre>');
check('闭合围栏后接正文 = 2 块', blockCount('```js\nlet a=1\n```\n\n后续'), 2);
check('列表连续项 = 1 块', blockCount('- a\n- b\n- c'), 1);

section('工具标记');
check('渲染成芯片', /chat-tool-chip/.test(renderMarkdown('::ToolCall::executeSql::select 1::')), true);
check('参数明细不泄漏到界面', /select 1/.test(renderMarkdown('::ToolCall::executeSql::select 1::')), false);
check('切点落在标记内会推到边界',
  alignMark('abc\n\n::ToolCall::executeSql::select 1::\n\n后续', 12) > 12, true);
check('切点在标记外不动', alignMark('普通文本很长很长', 5), 5);

// ── 3) 增量渲染不变式 ───────────────────────────────────────────
section('增量渲染不变式(逐字符驱动)');

// 覆盖各种块型 + 未闭合内联 + 工具标记 + 易误判文本
const DOC = [
  '## 库存查询结果', '',
  '为你查到 **3 件** 低于预警线的商品，明细如下：', '',
  '::ToolCall::executeSql::select * from product where stock < warn::', '',
  '| 商品 | 库存 | 预警线 |', '|---|---|---|', '| 飞机杯 | 2 | 10 |', '| 充电宝 | 5 | 20 |', '',
  '### 处理建议', '',
  '1. **优先补货**：库存低于 `3` 的商品', '2. 复核供应商 *交期*', '',
  '```sql', 'select * from product where stock < 3;', '```', '',
  '合计金额 3,998.00 元，计算式 3 * 4 * 5 仅作示例。',
].join('\n');

const container = new El('div');
const scroller = Object.assign(new El('div'), { scrollHeight: 100, scrollTop: 100, clientHeight: 100 });
const renderer = createStreamRenderer(container, scroller);

let rewritten = null;   // 已提交块被改写的证据
let snapshot = [];
for (const ch of DOC) {
  renderer.push(ch);
  flushFrames();
  const tail = container.children[container.children.length - 1];
  const committed = container.children.filter(c => c !== tail).map(c => c._html);
  if (!rewritten) {
    if (committed.length < snapshot.length) {
      rewritten = `块数回退 ${snapshot.length} -> ${committed.length}`;
    } else {
      for (let i = 0; i < snapshot.length; i++) {
        if (committed[i] !== snapshot[i]) {
          rewritten = `块#${i} 被改写\n     旧: ${snapshot[i]}\n     新: ${committed[i]}`;
          break;
        }
      }
    }
  }
  snapshot = committed;
}
renderer.finish();
flushFrames();

check('已提交的块从不被改写', rewritten, null);
check('确实发生了增量提交(而非一次性渲染)', snapshot.length > 3, true);
check('tail 容器已解散', container.children.some(c => c.className === 'md-tail'), false);

const streamed = container.children.map(c => c._html).join('');
check('★ 流式最终结果 === 整段渲染结果', streamed, renderMarkdown(DOC));

section('最终内容抽查');
check('表格已渲染', /<table><tr><th>商品<\/th>/.test(streamed), true);
check('代码块已渲染', /<pre><code>select \* from product/.test(streamed), true);
check('工具芯片已渲染', /chat-tool-chip/.test(streamed), true);
check('SQL 明细未泄漏', /where stock &lt; warn/.test(streamed), false);
check('行内代码未被污染', /<code>3<\/code>/.test(streamed), true);
check('乘法式未被误斜体', /3 \* 4 \* 5/.test(streamed), true);

// ── 4) 性能(可选) ───────────────────────────────────────────────
if (process.argv.includes('--perf')) {
  section('每帧解析耗时(60fps 预算 16.7ms; 增量渲染每帧最多解析 2 次)');
  const unit = DOC + '\n\n';
  [1, 4, 10, 25].forEach(n => {
    const doc = unit.repeat(n);
    for (let i = 0; i < 20; i++) parseBlocks(closeOpenMarks(doc));   // 预热
    const t0 = process.hrtime.bigint();
    const ROUNDS = 200;
    for (let i = 0; i < ROUNDS; i++) parseBlocks(closeOpenMarks(doc));
    const ms = Number(process.hrtime.bigint() - t0) / 1e6 / ROUNDS;
    console.log(`  ${String(doc.length).padStart(6)} 字符 / ${String(blockCount(doc)).padStart(3)} 块`
      + `  ->  ${ms.toFixed(3)} ms/帧  ${ms * 2 < 16.7 ? 'OK' : '*** 超预算 ***'}`);
  });
}

// ── 汇总 ────────────────────────────────────────────────────────
console.log('');
if (failures.length) {
  failures.forEach(f => console.log('FAIL ' + f));
  console.log(`\n失败 ${failures.length} 项 / 通过 ${passed} 项`);
  process.exit(1);
}
console.log(`全部通过 (${passed} 项)`);
