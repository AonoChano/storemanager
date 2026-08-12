/**
 * 生成两周(2026-07-30 ~ 2026-08-12)销售演示数据
 * 运行: node gen_seed_sales.js > seed_sales_20260730_0812.sql
 * 输出: seed_sales_20260730_0812.sql (UTF-8)
 * 说明: 只新增数据(商品id 8+ / 供应商id 17+), 不动现有测试数据
 */
const fs = require('fs');

/* ---------------- 可复现随机 ---------------- */
function mulberry32(a) {
  return function () {
    a |= 0; a = (a + 0x6D2B79F5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}
const rnd = mulberry32(20260730);
const ri = (min, max) => Math.floor(rnd() * (max - min + 1)) + min;   // 含两端
const pick = (arr) => arr[Math.floor(rnd() * arr.length)];
const money = (n) => Math.round(n * 100) / 100;
// 本地时间格式化(不能用 toISOString, 那是 UTC 会差 8 小时)
const fmt = (ms) => {
  const d = new Date(ms);
  const p = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
};

/* ---------------- 基础数据 ---------------- */
// 分类复用现有: 1饮料 2零食 3日用品 4生鲜
const products = [
  // id, name, spec, unit, 进价, 售价, 分类, 预警
  [8,  '农夫山泉矿泉水', '550ml/瓶', '瓶', 1.20, 2.00, 1, 24],
  [9,  '可口可乐', '500ml/瓶', '瓶', 2.20, 3.00, 1, 24],
  [10, '雪碧', '500ml/瓶', '瓶', 2.20, 3.00, 1, 24],
  [11, '康师傅冰红茶', '500ml/瓶', '瓶', 2.00, 3.00, 1, 24],
  [12, '王老吉凉茶', '310ml/罐', '罐', 2.80, 4.00, 1, 24],
  [13, '红牛维生素功能饮料', '250ml/罐', '罐', 4.50, 6.00, 1, 12],
  [14, '青岛啤酒', '500ml/听', '听', 2.50, 3.50, 1, 24],
  [15, '蒙牛纯牛奶', '250ml/盒', '盒', 2.30, 3.00, 1, 24],
  [16, '乐事原味薯片', '70g/袋', '袋', 4.20, 5.50, 2, 12],
  [17, '奥利奥夹心饼干', '97g/盒', '盒', 4.50, 6.00, 2, 12],
  [18, '旺旺雪饼', '84g/袋', '袋', 3.50, 5.00, 2, 12],
  [19, '恰恰香瓜子', '160g/袋', '袋', 6.00, 8.00, 2, 12],
  [20, '双汇王中王火腿肠', '30g×10/袋', '袋', 9.00, 12.00, 2, 10],
  [21, '卫龙大面筋辣条', '106g/袋', '袋', 3.00, 4.00, 2, 12],
  [22, '徐福记沙琪玛', '469g/包', '包', 9.50, 13.00, 2, 8],
  [23, '三只松鼠每日坚果', '750g/盒', '盒', 35.00, 49.00, 2, 6],
  [24, '心相印抽纸', '3层100抽/包', '包', 2.80, 4.00, 3, 24],
  [25, '维达卷纸', '4层140g×10/提', '提', 18.00, 25.00, 3, 8],
  [26, '蓝月亮洗衣液', '1kg/瓶', '瓶', 13.00, 18.00, 3, 6],
  [27, '舒肤佳香皂', '115g/块', '块', 3.20, 4.50, 3, 12],
  [28, '黑人牙膏', '90g/支', '支', 7.00, 10.00, 3, 8],
  [29, '六神花露水', '180ml/瓶', '瓶', 8.00, 12.00, 3, 6],
  [30, '南孚5号电池', '4粒/板', '板', 5.50, 8.00, 3, 8],
  [31, '散装鸡蛋', '斤', '斤', 4.00, 5.50, 4, 20],
  [32, '红富士苹果', '斤', '斤', 3.50, 5.00, 4, 20],
  [33, '香蕉', '斤', '斤', 2.50, 3.50, 4, 15],
  [34, '西红柿', '斤', '斤', 2.00, 3.00, 4, 15],
  [35, '五花肉', '斤', '斤', 13.00, 16.00, 4, 10],
];

const suppliers = [
  [17, '武汉市恒源商贸有限公司', '陈志远', '13907123456', '武汉市江汉区解放大道666号'],
  [18, '城东农副产品配送中心', '刘梅', '13871234567', '武汉市武昌区武珞路88号'],
];

// 客户: 前 20 个是散客(零售小单), 后面是周边小店(批发性大单)
const retailCustomers = ['张伟','王芳','李娜','刘洋','陈静','赵磊','孙丽','周强','吴敏','郑浩','冯雪','蒋涛','韩梅','杨帆','朱琳','秦刚','许娜','何军','吕婷','张翠花','王建国','李秀英','刘桂香','陈大勇','黄淑芬','宋丽娟','邓超','彭丽华','罗军','肖红'];
const shopCustomers  = ['幸福便利店','家和超市','老张烟酒行','阳光文具店','惠民五金店','晨光文具店','老王水果摊'];

/* ---------------- 时间线: 采购铺货(7-28) + 补货(8-6) ---------------- */
const dayMs = 86400000;
const at = (str) => new Date(str).getTime();

// 首批铺货: 7-28 上午两单(饮料零食 / 日用品生鲜), 供应商17/18
const purchasePlans = [
  {
    no: 'P0' + at('2026-07-28T09:20:00'),
    time: '2026-07-28 09:20:00',
    supplierId: 17,
    remark: '首批进货-饮料零食',
    items: products.filter(p => p[6] <= 2).map(p => [p[0], ri(60, 120), p[4]]),
  },
  {
    no: 'P0' + at('2026-07-28T10:05:00'),
    time: '2026-07-28 10:05:00',
    supplierId: 18,
    remark: '首批进货-日用品生鲜',
    items: products.filter(p => p[6] >= 3).map(p => [p[0], p[6] === 4 ? ri(55, 95) : ri(40, 80), p[4]]),
  },
];

// 8-6 周中补货: 销量大的商品进一批
const refillIds = [8,9,10,11,13,16,20,22,24,25,26,27,31,32,33,34];

/* ---------------- 销售订单生成 (7-30 ~ 8-12) ---------------- */
function isWeekend(d) { const w = new Date(d).getDay(); return w === 0 || w === 6; }
function genDayOrders(dateStr) {
  const base = new Date(dateStr + 'T09:00:00').getTime();
  const count = isWeekend(dateStr) ? ri(8, 10) : ri(5, 7);
  const orders = [];
  // 营业时间 09:00-21:30, 生成 count 个递增时刻
  const slots = [];
  for (let i = 0; i < count; i++) slots.push(ri(0, 750)); // 距 9:00 的分钟数(营业至 21:30)
  slots.sort((a, b) => a - b);
  for (const m of slots) {
    const ts = base + m * 60000 + ri(0, 59) * 1000;
    const isShop = rnd() < 0.18; // 18% 周边小店批发单
    const customer = isShop ? pick(shopCustomers) : pick(retailCustomers);
    const itemCount = isShop ? ri(2, 4) : ri(1, 3);
    const items = [];
    for (let k = 0; k < itemCount; k++) {
      const p = pick(products);
      const qty = isShop ? ri(3, 15) : (p[6] === 4 ? ri(2, 8) : ri(1, 4));
      items.push({ pid: p[0], qty, price: p[5] });
    }
    const total = money(items.reduce((s, it) => s + it.price * it.qty, 0));
    // 5% 概率留个备注, 其余为空
    const remark = rnd() < 0.05 ? pick(['老客户', '朋友拿货', '微信下单自提']) : null;
    orders.push({
      no: 'S0' + ts,
      time: fmt(ts),
      customer, total, items, remark,
    });
  }
  return orders;
}

const allOrders = [];
for (let d = new Date('2026-07-30'); d <= new Date('2026-08-12'); d = new Date(d.getTime() + dayMs)) {
  const dateStr = fmt(d.getTime()).slice(0, 10);
  allOrders.push(...genDayOrders(dateStr));
}

/* ---------------- 按时间线推演库存 (采购入库/销售出库全局按时间排序) ---------------- */
const stock = {};                 // pid -> 当前库存
const stockRecords = [];          // 已生成流水
const purchaseOrders = [];
const saleOrders = [];

// 首批铺货入库
for (const plan of purchasePlans) {
  let total = 0;
  const dets = plan.items.map(([pid, qty, price]) => {
    const amount = money(qty * price);
    total = money(total + amount);
    stock[pid] = (stock[pid] || 0) + qty;
    return { pid, qty, price, amount };
  });
  purchaseOrders.push({ ...plan, total, dets });
  for (const d of dets) {
    stockRecords.push({ pid: d.pid, type: 1, qty: d.qty, after: stock[d.pid], biz: '采购入库', no: plan.no, time: plan.time });
  }
}

// 销售明细金额
for (const o of allOrders) {
  const dets = o.items.map(it => ({ ...it, amount: money(it.price * it.qty) }));
  saleOrders.push({ ...o, dets });
}

// 按销量估算补货量: 保证补货后到 8-12 结束库存 >= 预警*1.2, 否则加量
const consumption = {};
for (const o of saleOrders) for (const d of o.dets) consumption[d.pid] = (consumption[d.pid] || 0) + d.qty;
const refillPlan = {
  no: 'P0' + at('2026-08-06T08:40:00'),
  time: '2026-08-06 08:40:00',
  supplierId: 17,
  remark: '周中补货',
  items: refillIds.map(id => {
    const p = products.find(x => x[0] === id);
    // 补货前剩余 = 首批 - 8-6 前消耗; 需保证最终库存 >= 预警*1.2
    const soldBefore = saleOrders.filter(o => o.time < '2026-08-06').reduce((s, o) => s + o.dets.filter(d => d.pid === id).reduce((a, b) => a + b.qty, 0), 0);
    const before = (stock[id] || 0) - soldBefore;
    const need = Math.max(p[7] * 1.2 + (consumption[id] - soldBefore) - before, 0);
    const qty = Math.max(ri(40, 100), need);
    return [id, qty, p[4]];
  }),
};

// 补货入库
{
  let total = 0;
  const dets = refillPlan.items.map(([pid, qty, price]) => {
    const amount = money(qty * price);
    total = money(total + amount);
    stock[pid] = (stock[pid] || 0) + qty;
    return { pid, qty, price, amount };
  });
  purchaseOrders.push({ ...refillPlan, total, dets });
  for (const d of dets) {
    stockRecords.push({ pid: d.pid, type: 1, qty: d.qty, after: stock[d.pid], biz: '采购入库', no: refillPlan.no, time: refillPlan.time });
  }
}

// 销售出库 -> 流水 (与采购混合按时间排序, 保证 after_stock 真实)
const timeline = [
  ...stockRecords.map(r => ({ t: r.time, kind: 'in', r })),
  ...saleOrders.map(o => o.dets.map(d => ({ t: o.time, kind: 'out', o, d }))).flat(),
].sort((a, b) => a.t.localeCompare(b.t));

stockRecords.length = 0;
for (const ev of timeline) {
  if (ev.kind === 'in') {
    stockRecords.push(ev.r); // after 已在入库时算好
  } else {
    stock[ev.d.pid] -= ev.d.qty;
    stockRecords.push({ pid: ev.d.pid, type: 0, qty: ev.d.qty, after: stock[ev.d.pid], biz: '销售出库', no: ev.o.no, time: ev.o.time });
  }
}

/* ---------------- 输出 SQL ---------------- */
const sql = [];
const q = (s) => "'" + String(s ?? '').replace(/'/g, "''") + "'";

sql.push('-- 两周销售演示数据 2026-07-30 ~ 2026-08-12 (生成脚本 gen_seed_sales.js)');
sql.push('-- 幂等前提: 商品id>=8, 供应商id>=17, 单号唯一; 请勿重复执行同一批文件');
sql.push('SET NAMES utf8mb4;');
sql.push('');

// 供应商
sql.push('-- 供应商');
for (const [id, name, contact, phone, addr] of suppliers) {
  sql.push(`INSERT INTO supplier (id, name, contact, phone, address, remark, create_time) VALUES (${id}, ${q(name)}, ${q(contact)}, ${q(phone)}, ${q(addr)}, ${q('演示数据')}, '2026-07-25 10:00:00');`);
}
sql.push('');

// 商品
sql.push('-- 商品 (create_time 在铺货之前)');
for (const [id, name, spec, unit, pp, sp, cat, warn] of products) {
  sql.push(`INSERT INTO product (id, name, barcode, category_id, spec, unit, purchase_price, sale_price, stock, warning_threshold, status, create_time) VALUES (${id}, ${q(name)}, NULL, ${cat}, ${q(spec)}, ${q(unit)}, ${pp}, ${sp}, ${stock[id]}, ${warn}, 1, '2026-07-26 09:00:00');`);
}
sql.push('');

// 采购单(已入库), 显式 id 从 6 开始(现有 1-5)
let poId = 5;
for (const po of purchaseOrders) {
  po.id = ++poId;
  sql.push(`INSERT INTO purchase_order (id, order_no, supplier_id, total_amount, status, operator_id, remark, create_time) VALUES (${po.id}, ${q(po.no)}, ${po.supplierId}, ${po.total}, 1, 1, ${q(po.remark)}, ${q(po.time)});`);
  for (const d of po.dets) {
    sql.push(`INSERT INTO purchase_order_detail (order_id, product_id, quantity, price, amount) VALUES (${po.id}, ${d.pid}, ${d.qty}, ${d.price}, ${d.amount});`);
  }
}
sql.push('');

// 销售单(已出库), 显式 id 从 4 开始(现有 1-3)
let soId = 3;
for (const o of saleOrders) {
  o.id = ++soId;
  sql.push(`INSERT INTO sale_order (id, order_no, customer_name, total_amount, status, operator_id, remark, create_time) VALUES (${o.id}, ${q(o.no)}, ${q(o.customer)}, ${o.total}, 1, 1, ${q(o.remark)}, ${q(o.time)});`);
  for (const d of o.dets) {
    sql.push(`INSERT INTO sale_order_detail (order_id, product_id, quantity, price, amount) VALUES (${o.id}, ${d.pid}, ${d.qty}, ${d.price}, ${d.amount});`);
  }
}
sql.push('');

// 库存流水 (时间升序)
sql.push('-- 库存流水(时间升序)');
for (const r of stockRecords.sort((a, b) => a.time.localeCompare(b.time))) {
  sql.push(`INSERT INTO stock_record (product_id, type, quantity, after_stock, biz_type, order_no, operator_id, create_time) VALUES (${r.pid}, ${r.type}, ${r.qty}, ${r.after}, ${q(r.biz)}, ${q(r.no)}, 1, ${q(r.time)});`);
}

fs.writeFileSync(__dirname + '/seed_sales_20260730_0812.sql', sql.join('\n') + '\n', 'utf8');

/* ---------------- 统计输出 ---------------- */
const totalSales = money(saleOrders.reduce((s, o) => s + o.total, 0));
const totalBuy = money(purchaseOrders.reduce((s, o) => s + o.total, 0));
console.log(`销售单: ${saleOrders.length} 张, 明细 ${saleOrders.reduce((s,o)=>s+o.dets.length,0)} 条, 总销售额 ¥${totalSales.toFixed(2)}`);
console.log(`采购单: ${purchaseOrders.length} 张, 总进货 ¥${totalBuy.toFixed(2)}`);
console.log(`流水: ${stockRecords.length} 条`);
const low = products.filter(p => stock[p[0]] <= p[7]);
console.log(`低库存商品(<=预警): ${low.map(p => p[1] + '(' + stock[p[0]] + ')').join(', ') || '无'}`);
const salesByDay = {};
for (const o of saleOrders) { const d = o.time.slice(0, 10); salesByDay[d] = (salesByDay[d] || 0) + 1; }
console.log('每日单量:', Object.entries(salesByDay).map(([d, n]) => `${d}:${n}单`).join(' '));
console.log('SQL 文件已写出: seed_sales_20260730_0812.sql');
