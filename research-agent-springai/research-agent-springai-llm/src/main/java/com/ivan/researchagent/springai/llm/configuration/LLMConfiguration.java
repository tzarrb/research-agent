package com.ivan.researchagent.springai.llm.configuration;

import com.ivan.researchagent.springai.llm.memory.database.DatabaseChatMemory;
import com.ivan.researchagent.springai.llm.memory.database.DatabaseChatMemoryRepository;
import okhttp3.OkHttpClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
//@AutoConfiguration(after = {LettuceRedisChatMemoryConnectionAutoConfiguration.class, JedisRedisChatMemoryConnectionAutoConfiguration.class})
public class LLMConfiguration {

//    @Resource
//    private RedisTemplate<String, Message> redisTemplate;

    @Bean
    public ChatMemory customChatMemory(DatabaseChatMemoryRepository chatMemoryRepository) {
        //return new RedisChatMemory(redisTemplate);

//        ChatMemory chatMemory = MessageWindowChatMemory.builder()
//                .chatMemoryRepository(chatMemoryRepository)
//                .maxMessages(100)
//                .build();

        ChatMemory chatMemory = new DatabaseChatMemory(chatMemoryRepository, 100);

        return chatMemory;
    }

    @Bean
    public MessageChatMemoryAdvisor customMessageChatMemoryAdvisor(ChatMemory customChatMemory) {
        return MessageChatMemoryAdvisor.builder(customChatMemory).build();
    }

    @Bean
    public PromptChatMemoryAdvisor customPromptChatMemoryAdvisor(ChatMemory customChatMemory) {
        return PromptChatMemoryAdvisor.builder(customChatMemory).build();
    }

    @Bean
    public RestClient okHttpRestClient() {
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
