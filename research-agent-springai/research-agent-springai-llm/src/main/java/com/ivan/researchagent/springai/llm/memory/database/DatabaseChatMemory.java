package com.ivan.researchagent.springai.llm.memory.database;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/18/周一
 **/
public class DatabaseChatMemory implements ChatMemory {

    private final DatabaseChatMemoryRepository repository;
    private final int maxMessages; // 最大记忆条数（最近 N 条）

    public DatabaseChatMemory(DatabaseChatMemoryRepository repository, int maxMessages) {
        this.repository = repository;
        this.maxMessages = maxMessages;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        repository.saveAll(conversationId, messages);
    }

    @Override
    public List<Message> get(String conversationId) {
        // 获取最近 maxMessages 条消息作为上下文
        return repository.findByConversationId(conversationId, maxMessages);
    }

    @Override
    public void clear(String conversationId) {
        repository.deleteByConversationId(conversationId);
    }
}
