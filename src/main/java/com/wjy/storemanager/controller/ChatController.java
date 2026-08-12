package com.wjy.storemanager.controller;

import com.wjy.storemanager.common.Result;
import com.wjy.storemanager.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // 客服对话(兼容旧版, 单轮): POST /chat  body: {"message":"xxx"}
    @PostMapping
    public Result<String> chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Result.error("请输入内容");
        }
        return Result.success(chatService.chat(message));
    }

    // 客服对话(流式+多轮): POST /chat/stream
    // body: {"message":"xxx", "history":[{"role":"user|assistant","content":"..."}]}
    // 响应: SSE, 每块 data: 一段文本, 结束 data: [DONE], 出错 data: [ERROR]...
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody Map<String, Object> body,
                             @RequestHeader(value = "X-User-Name", required = false) String userName) {
        String message = body.get("message") == null ? "" : String.valueOf(body.get("message")).trim();
        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) body.getOrDefault("history", List.of());

        SseEmitter emitter = new SseEmitter(0L);
        if (message.isEmpty()) {
            sendSafe(emitter, "[ERROR]请输入内容");
            emitter.complete();
            return emitter;
        }

        chatService.streamChat(message, history, userName).subscribe(
                chunk -> {
                    // 思考链增量: [THINK] 前缀, 前端渲染滚筒/折叠的思考过程块
                    if (chunk.reasoning() != null && !chunk.reasoning().isEmpty()) {
                        sendSafe(emitter, "[THINK]" + chunk.reasoning());
                    }
                    // 工具调用: [TOOL]工具名 或 [TOOL]工具名::参数明细, 前端渲染银灰芯片
                    if (chunk.toolName() != null && !chunk.toolName().isBlank()) {
                        String detail = chunk.toolDetail();
                        sendSafe(emitter, "[TOOL]" + chunk.toolName()
                                + (detail != null && !detail.isBlank() ? "::" + detail : ""));
                    }
                    // 正文增量: 无前缀, 前端打字机渲染
                    if (chunk.content() != null && !chunk.content().isEmpty()) {
                        sendSafe(emitter, chunk.content());
                    }
                },
                error -> {
                    sendSafe(emitter, "[ERROR]" + error.getMessage());
                    emitter.complete();
                },
                () -> {
                    sendSafe(emitter, "[DONE]");
                    emitter.complete();
                }
        );
        return emitter;
    }

    private void sendSafe(SseEmitter emitter, String data) {
        try {
            emitter.send(SseEmitter.event().data(data));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }
}
