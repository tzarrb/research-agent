package com.ivan.researchagent.springai.llm.advisors;

import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClient;

import java.util.function.Consumer;

import static org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor.TOP_K;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/18/周一
 **/
public class ChatMemoryAdvisorSpec implements Consumer<ChatClient.AdvisorSpec> {

    //对话记忆的返回条数
    private static final Integer CHAT_MEMORY_RETRIEVE_SIZE = 100;

    private String conversantId;

    public ChatMemoryAdvisorSpec(String conversantId) {
        this.conversantId = conversantId;
    }

    @Override
    public void accept(ChatClient.AdvisorSpec spec) {
        spec.param(CONVERSATION_ID, conversantId)
                .param(TOP_K, CHAT_MEMORY_RETRIEVE_SIZE);
    }

    @NotNull
    @Override
    public Consumer<ChatClient.AdvisorSpec> andThen(@NotNull Consumer<? super ChatClient.AdvisorSpec> after) {
        return Consumer.super.andThen(after);
    }
}
