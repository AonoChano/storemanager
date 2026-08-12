package com.wjy.storemanager.service;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * 工具调用通知通道: 流式对话时, 把"AI 正在调用工具"事件转发给当前 SSE 流。
 * 背景: Spring AI 的流式工具调用在框架内部被消费(工具调用阶段的 chunk 不经过 chatResponse 流),
 * 只能从工具执行侧主动通知。ChatService 在每次流式请求开始时 setListener, 结束后 clear。
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
