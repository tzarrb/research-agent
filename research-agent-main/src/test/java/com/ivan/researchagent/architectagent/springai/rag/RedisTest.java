package com.ivan.researchagent.architectagent.springai.rag;

import com.google.common.collect.Lists;
import com.ivan.researchagent.main.AgentApplication;
import com.ivan.researchagent.springai.llm.model.chat.ChatMessage;
import jakarta.annotation.Resource;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/4/18/周五
 **/
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = AgentApplication.class)
public class RedisTest {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void test() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUserMessage("你好");

        ChatMessage chatMessage1 = new ChatMessage();
        chatMessage1.setUserMessage("hello world");

        redisTemplate.opsForList().rightPushAll("test", Lists.newArrayList(chatMessage, chatMessage1));

        List<Object> objects = redisTemplate.opsForList().range("test", 0, -1);
        List<ChatMessage> chatMessages = objects.stream()
                .map(obj -> (ChatMessage)obj)
                .collect(java.util.stream.Collectors.toList());
        System.out.println(chatMessages);
    }
}
