package com.ivan.researchagent.architectagent.springai.rag;

import com.google.common.collect.Lists;
import com.ivan.researchagent.main.AgentApplication;
import com.ivan.researchagent.springai.llm.model.chat.ChatRequest;
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
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setUserMessage("你好");

        ChatRequest chatRequest1 = new ChatRequest();
        chatRequest1.setUserMessage("hello world");

        redisTemplate.opsForList().rightPushAll("test", Lists.newArrayList(chatRequest, chatRequest1));

        List<Object> objects = redisTemplate.opsForList().range("test", 0, -1);
        List<ChatRequest> chatRequests = objects.stream()
                .map(obj -> (ChatRequest)obj)
                .collect(java.util.stream.Collectors.toList());
        System.out.println(chatRequests);
    }
}
