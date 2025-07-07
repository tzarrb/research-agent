package com.ivan.researchagent.main.controller;

import com.google.common.collect.Lists;
import com.ivan.researchagent.common.constant.Constant;
import com.ivan.researchagent.main.model.chat.ChatInput;
import com.ivan.researchagent.springai.llm.model.chat.ChatRequest;
import com.ivan.researchagent.springai.llm.model.chat.ChatResult;
import com.ivan.researchagent.springai.llm.service.ChatService;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
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

    private final ChatService chatService;

//    @Resource
//    private List<McpSyncClient> mcpSyncClients;  // For sync client
    @Resource
    private List<McpAsyncClient> mcpAsyncClients;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;

    }

    @GetMapping("")
    @Operation(summary = "聊天", description = "返回聊天消息")
    public String chat(@RequestBody ChatRequest chatRequest, HttpServletRequest request, HttpServletResponse response) {

        String sessionId = chatRequest.getSessionId();
        if (StringUtils.isBlank(sessionId)) {
            sessionId = request.getHeader("sessionId");
            chatRequest.setSessionId(sessionId);
        }

        ChatResult chatResult = chatService.chat(chatRequest);

        response.setHeader("sessionId", chatResult.getSessionId());
        return chatResult.getContent();
    }

    @GetMapping("/chat")
    @Operation(summary = "聊天-简单参数", description = "返回聊天消息")
    public String chatGet(@RequestParam String userMessage, HttpServletRequest request, HttpServletResponse response) {
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setProvider("dashscope");
        chatRequest.setModel("qwen-max");
        chatRequest.setSystemMessage(systemPrompt);
        chatRequest.setUserMessage(userMessage);
        chatRequest.setEnableMemory(true);
        chatRequest.setEnableStream(false);
        chatRequest.setEnableAgent(false);
        chatRequest.setEnableLocal(true);

        AsyncMcpToolCallbackProvider toolCallbackProvider = new AsyncMcpToolCallbackProvider(mcpAsyncClients);
        //SyncMcpToolCallbackProvider toolCallbackProvider = new SyncMcpToolCallbackProvider(mcpSyncClients);
        chatRequest.setToolCallbackProviders(Lists.newArrayList(toolCallbackProvider));

        String sessionId = request.getHeader(Constant.SESSION_ID);
        chatRequest.setSessionId(sessionId);

        ChatResult chatResult = chatService.chat(chatRequest);
        response.setHeader(Constant.SESSION_ID, chatResult.getSessionId());
        return chatResult.getContent();
    }

    @GetMapping("/stream")
    @Operation(summary = "流式聊天", description = "返回流式聊天消息")
    public Flux<String> streamChat(@RequestBody ChatRequest chatRequest, HttpServletRequest request, HttpServletResponse response) {
        AsyncMcpToolCallbackProvider toolCallbackProvider = new AsyncMcpToolCallbackProvider(mcpAsyncClients);
        //SyncMcpToolCallbackProvider toolCallbackProvider = new SyncMcpToolCallbackProvider(mcpSyncClients);
        chatRequest.setToolCallbackProviders(Lists.newArrayList(toolCallbackProvider));

        String sessionId = request.getHeader(Constant.SESSION_ID);
        chatRequest.setSessionId(sessionId);

        Flux<ChatResult> chatResult = chatService.steam(chatRequest);

        return chatResult.map(result -> {
            log.info("sessionId:{}, streamChat result:{}", result.getSessionId(), result.getContent());
            response.setHeader(Constant.SESSION_ID, result.getSessionId());
            return result.getContent();
        });
    }

    @GetMapping("/stream/chat")
    @Operation(summary = "流式聊天-简单参数", description = "返回流式聊天消息")
    public Flux<String> steamChatGet(@RequestParam String userMessage, HttpServletRequest request, HttpServletResponse response) {
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setProvider("dashscope");
        chatRequest.setModel("qwen-max");
        chatRequest.setSystemMessage(systemPrompt);
        chatRequest.setUserMessage(userMessage);
        chatRequest.setEnableMemory(true);
        chatRequest.setEnableStream(true);
        chatRequest.setEnableAgent(false);

        AsyncMcpToolCallbackProvider toolCallbackProvider = new AsyncMcpToolCallbackProvider(mcpAsyncClients);
        //SyncMcpToolCallbackProvider toolCallbackProvider = new SyncMcpToolCallbackProvider(mcpSyncClients);
        chatRequest.setToolCallbackProviders(Lists.newArrayList(toolCallbackProvider));

        String sessionId = request.getHeader(Constant.SESSION_ID);
        chatRequest.setSessionId(sessionId);

        Flux<ChatResult> chatResult = chatService.steam(chatRequest);

        return chatResult.map(result -> {
            log.info("sessionId:{}, streamChat result:{}", result.getSessionId(), result.getContent());
            response.setHeader(Constant.SESSION_ID, result.getSessionId());
            return result.getContent();
        });
    }


    @GetMapping("/sse/chat")
    @Operation(summary = "SSE流式聊天", description = "返回流式聊天消息")
    public SseEmitter sseChatGet(ChatInput userInput, HttpServletRequest request, HttpServletResponse response) {
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setSystemMessage(systemPrompt);
        chatRequest.setUserMessage(userInput.getUserMessage());
        chatRequest.setEnableWeb(userInput.getEnableWeb());
        chatRequest.setEnableLocal(userInput.getEnableLocal());
        chatRequest.setEnableMemory(true);
        chatRequest.setEnableStream(true);
        chatRequest.setEnableAgent(false);

        AsyncMcpToolCallbackProvider toolCallbackProvider = new AsyncMcpToolCallbackProvider(mcpAsyncClients);
        //SyncMcpToolCallbackProvider toolCallbackProvider = new SyncMcpToolCallbackProvider(mcpSyncClients);
        chatRequest.setToolCallbackProviders(Lists.newArrayList(toolCallbackProvider));

        String sessionId = request.getHeader(Constant.SESSION_ID);
        chatRequest.setSessionId(sessionId);

        SseEmitter sseEmitter = chatService.sseChat(chatRequest);
        response.setHeader(Constant.SESSION_ID, chatRequest.getSessionId());

        return sseEmitter;
    }
}
