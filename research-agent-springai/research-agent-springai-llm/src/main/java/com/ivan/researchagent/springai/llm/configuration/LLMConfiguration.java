package com.ivan.researchagent.springai.llm.configuration;

import com.alibaba.cloud.ai.memory.redis.RedisChatMemoryRepository;
import com.ivan.researchagent.springai.llm.config.LLMConfig;
import com.ivan.researchagent.springai.llm.memory.RedisChatMemory;
import jakarta.annotation.Resource;
import okhttp3.OkHttpClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/25 10:15
 **/
@Configuration
public class LLMConfiguration {

//    @Resource
//    private RedisChatMemoryRepository redisChatMemoryRepository;

//    @Resource
//    private RedisTemplate<String, Message> redisTemplate;

//    @Bean
//    public ChatMemory chatMemory() {
//        return new InMemoryChatMemory();
//    }

    @Bean
    public ChatMemory redisChatMemory(RedisChatMemoryRepository redisChatMemoryRepository) {
//        return new RedisChatMemory(redisTemplate);

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(redisChatMemoryRepository)
                .maxMessages(100)
                .build();

        return chatMemory;
    }

    @Bean
    public MessageChatMemoryAdvisor redisMessageChatMemoryAdvisor(ChatMemory redisChatMemory) {
        return MessageChatMemoryAdvisor.builder(redisChatMemory).build();
    }

    @Bean
    public RestClient restClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(60))
                .readTimeout(Duration.ofSeconds(300))
                .writeTimeout(Duration.ofSeconds(300)) // 补充写入超时
                .retryOnConnectionFailure(true) // 启用连接失败自动重试
                .build();

        return RestClient.builder()
                .requestFactory(new OkHttp3ClientHttpRequestFactory(okHttpClient))
                .build();
    }
}
