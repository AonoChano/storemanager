package com.wjy.storemanager.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    // 流式返回的一块: reasoning=思考链增量(可空), content=正文增量(可空)
    // 思考链与正文在流中交替出现: 思考阶段 content 为空, 思考完 reasoning 为空
    public record ChatChunk(String reasoning, String content) {}

    // 明显跑题的关键词(黑名单, 第①层硬拦截): 命中直接拒绝, 不调AI
    // 换成黑名单的好处: 正常的进销存问题不管怎么问都能过, 只挡明显不相关的
    private static final String[] OFF_TOPIC_KEYWORDS = {
            "天气", "编程", "代码", "java", "python", "javascript", "笑话",
            "美食", "菜谱", "旅游", "电影", "电视剧", "音乐", "歌曲", "游戏",
            "体育", "足球", "篮球", "新闻", "政治", "股票", "基金", "化妆",
            "穿搭", "英语", "数学", "物理", "化学", "历史", "明星", "八卦",
            "相亲", "恋爱", "游戏机", "电脑配置"
    };

    // 第②层软约束: system prompt 让 AI 自己判断话题, 跑题就拒绝
    private static final String SYSTEM_PROMPT =
            "你是一个'进销存管理系统'的客服助手。\n" +
            "你可以回答的业务话题：采购、销售、库存、商品、供应商、客户、分类、报表、毛利、预警、条码、订单、进出货流程等。\n" +
            "规则：\n" +
            "1. 只有问题与进销存完全无关时（如天气、闲聊、娱乐、技术、生活），才回答：'我只能回答进销存相关的问题，比如采购、销售、库存、报表等。'\n" +
            "2. 对进销存相关的业务问题（如何采购、入库流程、怎么定价、库存管理方法、报表怎么看、预警怎么设置等），给出实用、具体、有条理的回答。\n" +
            "3. 如果用户问的是系统里的实时数据（商品进价/售价/库存、低库存预警、今日销售等），调用提供的查询工具获取真实数据，再基于真实数据回答；工具查不到就如实说明。\n" +
            "4. 回答简洁、专业，使用中文，不要编造虚假的具体数字。\n" +
            "5. 当用户问统计、聚合、排行、对比类问题（如'哪个商品毛利最高''按分类汇总库存''总销售额''销售Top5''某商品卖了多少'），必须使用 executeSql 工具写SQL查询整表数据，不要逐个查询单个商品，也不要凭少量数据猜测，更不要让用户去系统里自己看。\n" +
            "6. 输出格式（重要）：多步骤、列表、条目类回答必须每项单独一行，用换行符分隔，禁止把所有内容挤在同一行；强调文字用 **文字** 标记；Markdown 语法（# 标题、- 列表、1. 编号）正常使用。";

    private final ChatClient chatClient;

    // 构造注入: Spring AI 的 DeepSeek 自动配置会提供 ChatClient.Builder
    public ChatService(ChatClient.Builder builder, InventoryTools inventoryTools) {
        // defaultTools: 把查询工具注册给 AI, 它需要真实数据时会自动调用
        this.chatClient = builder.defaultTools(inventoryTools).build();
    }

    // 客服入口(兼容旧调用): 双层限制, 单轮
    public String chat(String message) {
        // ① 硬拦截(不调AI): 命中明显跑题词 → 直接拒绝
        if (isOffTopic(message)) {
            return "我只能回答进销存相关的问题，比如采购、销售、库存、报表等。";
        }
        // ② 软约束(调AI): system prompt 限定话题, 不相关的由 AI 拒绝
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(message)
                .call()
                .content();
    }

    // 客服入口(流式+多轮): 前端带最近对话历史, AI 能指代"刚才说的"
    // history 格式: [{role:"user"|"assistant", content:"..."}]
    // 返回: 每块含 thinking(思考链) 与 content(正文), 二者可能其一为空
    public Flux<ChatChunk> streamChat(String message, List<Map<String, String>> history, String userName) {
        // ① 硬拦截(不调AI)
        if (isOffTopic(message)) {
            return Flux.just(new ChatChunk(null, "我只能回答进销存相关的问题，比如采购、销售、库存、报表等。"));
        }

        // ② 组装多轮历史(截断到最后 20 条, 控制 token)
        List<Message> messages = new ArrayList<>();
        if (history != null) {
            int from = Math.max(0, history.size() - 20);
            for (int i = from; i < history.size(); i++) {
                Map<String, String> h = history.get(i);
                String role = h.get("role");
                String content = h.get("content");
                if (content == null || content.isBlank()) continue;
                if ("user".equals(role)) messages.add(new UserMessage(content));
                else if ("assistant".equals(role)) messages.add(new AssistantMessage(content));
            }
        }
        messages.add(new UserMessage(message));

        // ③ 带上当前登录用户, 让 AI 能针对性回答
        String sys = SYSTEM_PROMPT;
        if (userName != null && !userName.isBlank()) {
            sys = SYSTEM_PROMPT + "\n当前登录用户是" + userName + "，回答时可以用'您'称呼。";
        }

        // ④ 流式调用: 逐块取出 思考链(DeepSeekAssistantMessage.reasoningContent) + 正文
        return chatClient.prompt()
                .system(sys)
                .messages(messages)
                .stream()
                .chatResponse()
                .map(resp -> {
                    String reasoning = null;
                    org.springframework.ai.chat.messages.AssistantMessage out =
                            resp.getResult() != null ? resp.getResult().getOutput() : null;
                    // DeepSeek 模块把思考链存在自定义消息类里(非 metadata)
                    if (out instanceof org.springframework.ai.deepseek.DeepSeekAssistantMessage dsm) {
                        reasoning = dsm.getReasoningContent();
                    }
                    return new ChatChunk(reasoning, out != null ? out.getText() : null);
                });
    }

    // 判断问题是否含"明显跑题"关键词
    private boolean isOffTopic(String msg) {
        if (msg == null) return false;
        for (String kw : OFF_TOPIC_KEYWORDS) {
            if (msg.contains(kw)) return true;
        }
        return false;
    }
}
