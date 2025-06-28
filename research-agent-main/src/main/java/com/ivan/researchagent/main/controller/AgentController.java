package com.ivan.researchagent.main.controller;

import com.ivan.researchagent.springai.agent.agentic.router.RoutingAgent;
import com.ivan.researchagent.springai.llm.model.chat.ChatMessage;
import com.ivan.researchagent.springai.llm.model.chat.ChatResult;
import com.ivan.researchagent.springai.llm.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

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
@RequestMapping("/agent")
@CrossOrigin(origins = "*") // 支持所有来源的跨域请求
@Tag(name = "智能体控制器", description = "智能体控制器")
public class AgentController {

    @Resource
    private ChatService chatService;

    @Resource
    private RoutingAgent routingAgent;


    @GetMapping("")
    @Operation(summary = "聊天", description = "返回聊天消息")
    public String chat(@RequestBody ChatMessage chatMessage, HttpServletRequest request, HttpServletResponse response) {

        String sessionId = chatMessage.getSessionId();
        if (StringUtils.isBlank(sessionId)) {
            sessionId = request.getHeader("sessionId");
            chatMessage.setSessionId(sessionId);
        }

        ChatResult chatResult = routingAgent.call(chatMessage);

        response.setHeader("sessionId", chatResult.getSessionId());
        return chatResult.getContent();
    }

    @GetMapping("/chat")
    @Operation(summary = "聊天-简单参数", description = "返回聊天消息")
    public String chatMessage(String userMessage, HttpServletRequest request, HttpServletResponse response) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setProvider("dashscope");
        chatMessage.setModel("qwen-max");
        chatMessage.setUserMessage(userMessage);
        chatMessage.setEnableStream(true);
        chatMessage.setEnableMemory(true);
        chatMessage.setEnableAgent(true);

        String sessionId = request.getHeader("sessionId");
        chatMessage.setSessionId(sessionId);

        ChatResult chatResult = routingAgent.call(chatMessage);

        response.setHeader("sessionId", chatResult.getSessionId());
        return chatResult.getContent();
    }

    @GetMapping("/stream")
    @Operation(summary = "流式聊天", description = "返回流式聊天消息")
    public Flux<String> streamChat(@RequestBody ChatMessage chatMessage, HttpServletRequest request, HttpServletResponse response) {

        String sessionId = chatMessage.getSessionId();
        if (StringUtils.isBlank(sessionId)) {
            sessionId = request.getHeader("sessionId");
            chatMessage.setSessionId(sessionId);
        }

        Flux<ChatResult> chatResult = routingAgent.stream(chatMessage);

        return chatResult.map(result -> {
            log.info("sessionId:{}, streamChat result:{}", result.getSessionId(), result.getContent());
            response.setHeader("sessionId", result.getSessionId());
            return result.getContent();
        });
    }

    //@GetMapping(value = "/stream/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    @GetMapping(value = "/stream/chat")
    @Operation(summary = "流式聊天-简单参数", description = "返回流式聊天消息")
    public Flux<String> streamChatGet(String userMessage, HttpServletRequest request, HttpServletResponse response) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setProvider("dashscope");
        chatMessage.setModel("qwen-max");
        chatMessage.setUserMessage(userMessage);
        chatMessage.setEnableStream(true);
        chatMessage.setEnableMemory(true);
        chatMessage.setEnableAgent(true);

        String sessionId = request.getHeader("sessionId");
        chatMessage.setSessionId(sessionId);

        Flux<ChatResult> chatResult = routingAgent.stream(chatMessage);

        return chatResult.map(result -> {
            log.info("sessionId:{}, streamChat result:{}", result.getSessionId(), result.getContent());
            response.setHeader("sessionId", result.getSessionId());
            return result.getContent();
        });
    }

}
