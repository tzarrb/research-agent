package com.ivan.researchagent.springai.llm;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.ivan.researchagent.main.AgentApplication;
import com.ivan.researchagent.springai.llm.model.chat.ChatParams;
import com.ivan.researchagent.springai.llm.provider.ModelFactory;
import com.ivan.researchagent.springai.llm.service.ChatService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/9/3/周三
 **/
@ExtendWith(SpringExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AgentApplication.class)
public class ChatTest {

    private static final String DEFAULT_PROMPT = "你好，介绍下你自己！";

    private ChatClient dashScopeChatClient;

    @Resource
    private DashScopeChatModel dashScopeChatModel;

    @Resource
    private ModelFactory modelFactory;

    @Resource
    private ChatService chatService;

    @Test
    public void test() {
        // 测试
        testStream();
    }

    @Test
    public void testStream() {
        ChatParams chatParams = ChatParams.builder()
                .provider("dashscope")
                .model("qwen-max")
                .enableMemory(true)
                .enableStream(true)
                .enableWeb(false)
                .enableAgent(false)
                .build();
        dashScopeChatClient = chatService.getChatClient(chatParams);
        dashScopeChatClient.prompt(DEFAULT_PROMPT)
                .stream()
                .content()
                .map(item -> {
                    System.out.print(item);
                    return item;
                });
    }


    private void init() {

        // 构造时，可以设置 ChatClient 的参数
        // {@link org.springframework.ai.chat.client.ChatClient};
        this.dashScopeChatClient = ChatClient.builder(dashScopeChatModel)
                // 实现 Logger 的 Advisor
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withTopP(0.7)
                                .build()
                )
                .build();
    }

}
