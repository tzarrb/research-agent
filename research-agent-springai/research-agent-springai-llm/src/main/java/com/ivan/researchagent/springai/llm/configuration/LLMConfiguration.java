package com.ivan.researchagent.springai.llm.configuration;

import com.ivan.researchagent.springai.llm.memory.database.DatabaseChatMemory;
import com.ivan.researchagent.springai.llm.memory.database.DatabaseChatMemoryRepository;
import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

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

//    @Bean
//    public RestClient okHttpRestClient() {
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .connectTimeout(Duration.ofMinutes(10))
//                .readTimeout(Duration.ofMinutes(10))
//                .writeTimeout(Duration.ofMinutes(10)) // 补充写入超时
//                .retryOnConnectionFailure(true) // 启用连接失败自动重试
//                .build();
//
//        return RestClient.builder()
//                .requestFactory(new OkHttp3ClientHttpRequestFactory(okHttpClient))
//                .build();
//    }

    @Bean
    public RestClient.Builder restClientBuilder() {

        // 2. 创建 RequestConfig 并设置超时
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.of(10, TimeUnit.MINUTES)) // 设置连接超时
                .setResponseTimeout(Timeout.of(10, TimeUnit.MINUTES))
                .setConnectionRequestTimeout(Timeout.of(10, TimeUnit.MINUTES))
                .build();

        // 3. 创建 CloseableHttpClient 并应用配置
        HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();

        // 4. 使用 HttpComponentsClientHttpRequestFactory 包装 HttpClient
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        // 5. 创建 RestClient 并设置请求工厂
        return RestClient.builder().requestFactory(requestFactory);
    }

}
