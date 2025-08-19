package com.ivan.researchagent.springai.llm.memory.database;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ivan.researchagent.core.dao.entity.ChatMemory;
import com.ivan.researchagent.core.dao.repository.mysql.ChatMemoryMapper;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.Comparator;
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
//@Component
public class DatabaseChatMemoryRepository implements ChatMemoryRepository {

//    @Resource
    private ChatMemoryMapper chatMemoryMapper;

    public DatabaseChatMemoryRepository(ChatMemoryMapper chatMemoryMapper) {
        this.chatMemoryMapper = chatMemoryMapper;
    }

    @Override
    public List<String> findConversationIds() {
        return List.of();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        // 默认查询所有历史（不限制条数）
        return findByConversationId(conversationId, Integer.MAX_VALUE);
    }

    // 新增方法：按会话 ID 查询指定数量的历史消息（用于限制记忆长度）
    public List<Message> findByConversationId(String conversationId, int size) {
        // 按时间倒序查询最近的 size 条记录）
        List<ChatMemory> records = chatMemoryMapper.selectList(
                Wrappers.<ChatMemory>lambdaQuery()
                        .eq(ChatMemory::getConversationId, conversationId)
                        .orderByDesc(ChatMemory::getId)
                        .last("LIMIT " + size)
        );
        // 转换为 Spring AI 的 Message 对象
        return records.stream()
                .sorted(Comparator.comparing(ChatMemory::getId))// 按id正序排序，不然大模型可能会无法理解
                .map(record -> deserializeMessage(record.getType(), record.getContent()))
                .toList();
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        for (Message message : messages) {
            // 避免重复保存（通过 metadata 标记）
            if (message.getMetadata().containsKey("old")) {
                continue;
            }
            ChatMemory entity = new ChatMemory();
            entity.setConversationId(conversationId);
            entity.setType(message.getMessageType().getValue()); // "user", "assistant", etc.
            entity.setContent(message.getText());
            chatMemoryMapper.insert(entity);
        }
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        chatMemoryMapper.delete(
                Wrappers.<ChatMemory>lambdaQuery()
                        .eq(ChatMemory::getConversationId, conversationId)
        );
    }

    // 工具方法：将数据库记录转为 Message 对象
    private Message deserializeMessage(String type, String content) {
        Message message = switch (type) {
            case "user" -> new UserMessage(content);
            case "assistant" -> new AssistantMessage(content);
            case "system" -> new SystemMessage(content);
            default ->
                    new AssistantMessage("未知消息类型: " + content);
        };
        // 增加老数据标识，标记是从库中查询出来的历史数据
        message.getMetadata().put("old", 1);
        return message;
    }
}
