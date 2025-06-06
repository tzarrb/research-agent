package com.ivan.researchagent.springai.llm.memory;

import com.ivan.researchagent.common.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/4/17/周四
 **/
@Slf4j
public class RedisChatMemory implements ChatMemory {

    private RedisTemplate<String, Message> redisTemplate;

    public RedisChatMemory(RedisTemplate<String, Message> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void add(String conversationId, Message message) {
        ChatMemory.super.add(conversationId, message);
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        String redisKey = String.format(Constant.KEY_CHAT_MEMORY, conversationId);
        log.debug("Adding {} messages to memoryId: {}", messages.size(), conversationId);

        try {
            redisTemplate.opsForList().rightPushAll(redisKey, messages);


            // 可选：限制列表长度
            // Long currentSize = redisTemplate.opsForList().size(key);
            // if (currentSize != null && currentSize > defaultMaxMessages) {
            //     // 保留最新的 defaultMaxMessages 条消息
            //     redisTemplate.opsForList().trim(key, currentSize - defaultMaxMessages, -1);
            //     logger.debug("Trimmed memoryId {} to {} messages", memoryId, defaultMaxMessages);
            // }

            // 可选：设置或刷新过期时间
//            if (expirationSeconds > 0) {
//                redisTemplate.expire(redisKey, expirationSeconds, TimeUnit.SECONDS);
//                log.debug("Set expiration for memoryId {} to {} seconds", conversationId, expirationSeconds);
//            }
        } catch (Exception e) {
            log.error("Error adding messages to Redis for memoryId: {}", conversationId, e);
            // 可以考虑抛出自定义异常或进行其他错误处理
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        return get(conversationId, 100);
    }

    public List<Message> get(String conversationId, int lastN) {
        String redisKey = String.format(Constant.KEY_CHAT_MEMORY, conversationId);
        log.debug("Getting last {} messages for memoryId: {}", lastN, conversationId);

        if (lastN <= 0) {
            log.warn("Requested non-positive number of messages ({}) for memoryId: {}. Returning empty list.", lastN, conversationId);
            return new ArrayList<>();
        }

        try {
            List<Message> values = redisTemplate.opsForList().range(redisKey, -lastN, -1);
            if (CollectionUtils.isEmpty(values)) {
                return Collections.emptyList();
            }
            return values;

            // List<Message> messages = JSON.parseArray(JSON.toJSONString(values), Message.class);

//        List<Message> messages = values.stream()
//                .map(obj -> JSON.parseObject((String)obj, Message.class))
//                .collect(Collectors.toList());

//        List<Message> messages = values.stream()
//                .map(obj -> (Message)obj)
//                .collect(Collectors.toList());

//        List<Message> messages = values.stream()
//                .map(obj -> {
//                    try {
//                        ObjectMapper mapper = new ObjectMapper();
//                        String json = mapper.writeValueAsString(obj);  // 序列化
//                        Message message = mapper.readValue(json, Message.class);  // 反序列化
//                        return message;
//                    } catch (Exception e) {
//                        log.error("Failed to deserialize message: {}", obj, e);
//                        return null;
//                    }
//                })
//                .collect(Collectors.toList());

//        return messages;
        } catch (Exception e) {
            log.error("Error getting messages from Redis for memoryId: {}", conversationId, e);
            // 返回空列表或进行其他错误处理
            return new ArrayList<>();
        }
    }

    @Override
    public void clear(String conversationId) {
        String redisKey = String.format(Constant.KEY_CHAT_MEMORY, conversationId);

        log.debug("Clearing memory for memoryId: {}", conversationId);
        try {
            redisTemplate.delete(redisKey);
        } catch (Exception e) {
            log.error("Error clearing Redis memory for memoryId: {}", conversationId, e);
        }
    }
}
