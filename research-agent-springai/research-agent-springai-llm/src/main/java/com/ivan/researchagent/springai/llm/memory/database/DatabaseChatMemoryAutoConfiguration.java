package com.ivan.researchagent.springai.llm.memory.database;

import com.ivan.researchagent.core.dao.repository.mysql.ChatMemoryMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/18/周一
 **/
@Slf4j
@Configuration
@ConditionalOnProperty(
        prefix = "spring.ai.memory",
        name = {"jdbc"},
        havingValue = "mysql"
)
public class DatabaseChatMemoryAutoConfiguration {

    @Resource
    private ChatMemoryMapper chatMemoryMapper;

    @Bean
    @Qualifier("databaseChatMemoryRepository")
    @ConditionalOnMissingBean(
            name = {"databaseChatMemoryRepository"}
    )
    DatabaseChatMemoryRepository databaseChatMemoryRepository() {
        log.info("Configuring Database chat memory repository");
        return new DatabaseChatMemoryRepository(chatMemoryMapper);
    }

//    @Bean
//    DatabaseChatMemory databaseChatMemory(DatabaseChatMemoryRepository repository){
//        return new DatabaseChatMemory(repository, 100);
//    }
}
