package com.ivan.researchagent.springai.llm.memory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivan.researchagent.common.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/4/18/周五
 **/
@Slf4j
public class RedisCacheChatMemory implements ChatMemory {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RedisCacheChatMemory(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        String key = String.format(Constant.KEY_CHAT_MEMORY, conversationId);
        messages.forEach(msg -> {
            try {
                redisTemplate.opsForList().rightPush(key, serializeMessage(msg));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Message serialization failed", e);
            }
        });
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        String key = String.format(Constant.KEY_CHAT_MEMORY, conversationId);
        List<String> rawMessages = redisTemplate.opsForList().range(key, -lastN, -1);
        return rawMessages.stream()
                .map(this::deserializeMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        String key = String.format(Constant.KEY_CHAT_MEMORY, conversationId);
        redisTemplate.delete(key);
    }

    private String serializeMessage(Message message) throws JsonProcessingException {
        return objectMapper.writeValueAsString(message);
    }

    private Message deserializeMessage(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String messageType = root.get("messageType").asText();
            return switch (MessageType.valueOf(messageType)) {
                case USER -> objectMapper.treeToValue(root, UserMessage.class);
                case ASSISTANT -> objectMapper.treeToValue(root, AssistantMessage.class);
                case SYSTEM -> objectMapper.treeToValue(root, SystemMessage.class);
                default -> throw new IllegalArgumentException("Unsupported message type");
            };
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Message deserialization failed", e);
        }
    }
}
