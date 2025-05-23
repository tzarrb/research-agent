package com.ivan.researchagent.main.controller;

import io.github.pigmesh.ai.deepseek.core.DeepSeekClient;
import io.github.pigmesh.ai.deepseek.core.Json;
import io.github.pigmesh.ai.deepseek.core.chat.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.Function;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/2/28/周五
 **/
@Slf4j
@RestController
@RequestMapping("/deepseek")
public class DeepSeekController {
    @Autowired
    private DeepSeekClient deepSeekClient;

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatCompletionResponse> chat(String prompt) {
        return deepSeekClient.chatFluxCompletion(prompt);
    }

    @GetMapping(value = "/chat/advanced", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatCompletionResponse> chatAdvanced(String prompt) {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                // 模型选择，支持 DEEPSEEK_CHAT、DEEPSEEK_REASONER 等
                .model(ChatCompletionModel.DEEPSEEK_REASONER)
                // 添加用户消息
                .addUserMessage(prompt)
                // 设置最大生成 token 数，默认 2048
                .maxTokens(1000)
                // 设置响应格式，支持 JSON 结构化输出
                .responseFormat(ResponseFormatType.JSON_OBJECT) // 可选
        // function calling
        //.tools(...) // 可选
        .build();

        return deepSeekClient.chatFluxCompletion(request);
    }


    public final static HashMap<String, String> cache = new HashMap<>();

    @GetMapping(value = "/chat/advanced/cache", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatCompletionResponse> chatAdvancedCache(String prompt, String cacheCode) {
        log.info("cacheCode {}", cacheCode);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(ChatCompletionModel.DEEPSEEK_REASONER)
                .addUserMessage(prompt)
                .addAssistantMessage(elt.apply(cache.getOrDefault(cacheCode, "")))
                .addSystemMessage("你是一个专业的助手").maxCompletionTokens(5000).build();
        log.info("request {}", Json.toJson(request));
        // 只保留上一次回答内容
        cache.remove(cacheCode);
        return deepSeekClient.chatFluxCompletion(request).doOnNext(i -> {
            String content = choicesProcess.apply(i.choices());
            // 其他ELT流程
            cache.merge(cacheCode, content, String::concat);
        }).doOnError(e -> log.error("/chat/advanced error:{}", e.getMessage()));
    }


    Function<List<ChatCompletionChoice>, String> choicesProcess = list -> list.stream().map(e -> e.delta().content())
            .collect(Collectors.joining());

    Function<String, String> elt = s -> s.replaceAll("<think>[\\s\\S]*?</think>", "").replaceAll("\n", "");

}
