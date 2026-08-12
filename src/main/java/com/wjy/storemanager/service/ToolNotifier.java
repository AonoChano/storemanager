package com.wjy.storemanager.service;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * 工具调用通知通道: 流式对话时, 把"AI 正在调用工具"事件转发给当前 SSE 流。
 * 背景: Spring AI 的流式工具调用在框架内部被消费(工具调用阶段的 chunk 不经过 chatResponse 流),
 * 只能从工具执行侧主动通知。ChatService 在每次流式请求开始时 setListener, 结束后 clear。
 * 注意: 流式响应在 Reactor 网络线程处理(工具执行也在该线程), 与请求线程不同,
 * 因此必须用跨线程可见的 AtomicReference, 不能用 ThreadLocal。
 * 并发限制: 同一时刻只有一个活动对话(本项目单用户场景), 后发请求会覆盖先发请求的监听。
 */
public final class ToolNotifier {

    private static final AtomicReference<Consumer<String>> LISTENER = new AtomicReference<>();

    private ToolNotifier() {
    }

    public static void setListener(Consumer<String> listener) {
        LISTENER.set(listener);
    }

    public static void clear() {
        LISTENER.set(null);
    }

    public static void notify(String toolName) {
        Consumer<String> listener = LISTENER.get();
        if (listener != null) {
            listener.accept(toolName);
        }
    }
}
