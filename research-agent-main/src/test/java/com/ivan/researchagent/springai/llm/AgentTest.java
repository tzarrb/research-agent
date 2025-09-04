package com.ivan.researchagent.springai.llm;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.ivan.researchagent.main.AgentApplication;
import com.ivan.researchagent.springai.llm.model.rag.VectorStoreData;
import com.ivan.researchagent.springai.llm.provider.ModelFactory;
import com.ivan.researchagent.springai.llm.service.RagChunkingService;
import com.ivan.researchagent.springai.llm.service.VectorStoreService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/4/10/周四
 **/
//@RunWith(SpringJUnit4ClassRunner.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = AgentApplication.class)
public class AgentTest {

    private static final String DEFAULT_PROMPT = "你好，介绍下你自己！";

    private ChatClient dashScopeChatClient;

    @Resource
    private DashScopeChatModel dashScopeChatModel;

    @Resource
    private ModelFactory modelFactory;

    @Test
    public void test() {
        initChatClient();
        testStream();
    }

    @org.junit.Test
    public void testStream() {
        dashScopeChatClient.prompt(DEFAULT_PROMPT)
                .stream()
                .content()
                .map(item -> {
                    System.out.print(item);
                    return item;
                });
    }


    private void initChatClient() {

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
