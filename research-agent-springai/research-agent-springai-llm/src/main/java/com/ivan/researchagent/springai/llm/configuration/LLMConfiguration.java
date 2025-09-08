package com.ivan.researchagent.springai.llm.configuration;

import com.ivan.researchagent.springai.llm.memory.database.DatabaseChatMemory;
import com.ivan.researchagent.springai.llm.memory.database.DatabaseChatMemoryRepository;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

}
