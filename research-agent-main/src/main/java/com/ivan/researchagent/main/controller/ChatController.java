package com.ivan.researchagent.main.controller;

import com.google.common.collect.Lists;
import com.ivan.researchagent.common.constant.Constant;
import com.ivan.researchagent.main.model.chat.ChatRequest;
import com.ivan.researchagent.springai.llm.model.chat.ChatParams;
import com.ivan.researchagent.springai.llm.model.chat.ChatResult;
import com.ivan.researchagent.springai.llm.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/11/28 13:43
 **/
@Slf4j
@RestController
@RequestMapping("/chat")
@CrossOrigin(
        origins = "*", // 支持所有来源的跨域请求,可设置具体前端地址http://localhost:5173
        allowedHeaders = "*",
        exposedHeaders = {"sessionId"} // 暴露自定义Header
)
@Tag(name = "聊天体控制器", description = "聊天体控制器")
public class ChatController {

    private final String systemPrompt =  """
            你是智能助理，针对用户的提问你可以通过工具或联网获取对应的信息，并回答给用户，回答的语气要拟人化，真实自然，并使用中文；
            
            """;

    @Resource
    private ChatService chatService;

    @Resource
    private List<ToolCallbackProvider> toolCallbackProviders;

    @Resource
    ToolCallbackProvider commonToolCallbackProvider;
    @Resource
    ToolCallbackProvider asyncMcpToolCallbackProvider;

    @PostMapping("")
    @Operation(summary = "聊天", description = "返回聊天消息")
    public String chat(@RequestBody ChatRequest chatRequest, HttpServletRequest request, HttpServletResponse response) {
        ChatParams chatParams = chatRequest.convertParams();

        String sessionId = chatParams.getSessionId();
        if (StringUtils.isBlank(sessionId)) {
            sessionId = request.getHeader("sessionId");
            chatParams.setSessionId(sessionId);
        }

        ChatResult chatResult = chatService.chat(chatParams);

        response.setHeader("sessionId", chatResult.getSessionId());
        return chatResult.getContent();
    }

    @GetMapping("/chat")
    @Operation(summary = "聊天-简单参数", description = "返回聊天消息")
    public String chatGet(@RequestParam String userMessage, HttpServletRequest request, HttpServletResponse response) {
        ChatParams chatParams = new ChatParams();
        chatParams.addSystemMessage(systemPrompt);
        chatParams.addUserMessage(userMessage);
        chatParams.setEnableMemory(true);
        chatParams.setEnableStream(false);
        chatParams.setEnableAgent(false);
        chatParams.setEnableLocal(true);

        //chatParams.setToolCallbackProviders(toolCallbackProviders);
        chatParams.setToolCallbackProviders(Lists.newArrayList(commonToolCallbackProvider, asyncMcpToolCallbackProvider));

        String sessionId = request.getHeader(Constant.SESSION_ID);
        chatParams.setSessionId(sessionId);

        ChatResult chatResult = chatService.chat(chatParams);
        response.setHeader(Constant.SESSION_ID, chatResult.getSessionId());
        return chatResult.getContent();
    }

    @PostMapping("/stream")
    @Operation(summary = "流式聊天", description = "返回流式聊天消息")
    public Flux<String> streamChat(@RequestBody ChatRequest chatRequest, HttpServletRequest request, HttpServletResponse response) {
        ChatParams chatParams = chatRequest.convertParams();

        String sessionId = request.getHeader(Constant.SESSION_ID);
        chatParams.setSessionId(sessionId);

        //chatParams.setToolCallbackProviders(toolCallbackProviders);
        chatParams.setToolCallbackProviders(Lists.newArrayList(commonToolCallbackProvider, asyncMcpToolCallbackProvider));

        log.info("开始调用ChatService.steam方法，sessionId: {}", sessionId);
        Flux<ChatResult> chatResult = chatService.steam(chatParams);

        return chatResult.map(result -> {
            log.info("sessionId:{}, streamChat result:{}", result.getSessionId(), result.getContent());
            response.setHeader(Constant.SESSION_ID, result.getSessionId());
            return result.getContent();
        }).doOnError(error -> {
            log.error("流式聊天发生错误: ", error);
        }).doOnComplete(() -> {
            log.info("流式聊天完成，sessionId: {}", chatParams.getSessionId());
        });
    }

    @GetMapping("/stream/chat")
    @Operation(summary = "流式聊天-简单参数", description = "返回流式聊天消息")
    public Flux<String> steamChatGet(@RequestParam String userMessage, HttpServletRequest request, HttpServletResponse response) {
        ChatParams chatParams = new ChatParams();
        chatParams.addSystemMessage(systemPrompt);
        chatParams.addUserMessage(userMessage);
        chatParams.setEnableMemory(true);
        chatParams.setEnableStream(true);
        chatParams.setEnableAgent(false);

        //chatParams.setToolCallbackProviders(toolCallbackProviders);
        chatParams.setToolCallbackProviders(Lists.newArrayList(commonToolCallbackProvider, asyncMcpToolCallbackProvider));

        String sessionId = request.getHeader(Constant.SESSION_ID);
        chatParams.setSessionId(sessionId);

        Flux<ChatResult> chatResult = chatService.steam(chatParams);

        return chatResult.map(result -> {
            log.info("sessionId:{}, streamChat result:{}", result.getSessionId(), result.getContent());
            response.setHeader(Constant.SESSION_ID, result.getSessionId());
            return result.getContent();
        });

    }


    @PostMapping("/sse/chat")
    @Operation(summary = "SSE流式聊天", description = "返回流式聊天消息")
    public SseEmitter sseChatGet(@RequestBody ChatRequest chatRequest, HttpServletRequest request, HttpServletResponse response) {
        ChatParams chatParams = chatRequest.convertParams();
        chatParams.addSystemMessage(systemPrompt);

        String sessionId = request.getHeader(Constant.SESSION_ID);
        chatParams.setSessionId(sessionId);

        //chatParams.setToolNames(Lists.newArrayList("tavilySearchService"));
        //chatParams.setToolCallbackProviders(toolCallbackProviders);
        chatParams.setToolCallbackProviders(Lists.newArrayList(commonToolCallbackProvider, asyncMcpToolCallbackProvider));

        SseEmitter sseEmitter = chatService.sseChat(chatParams);
        response.setHeader(Constant.SESSION_ID, chatParams.getSessionId());

        return sseEmitter;
    }
}
