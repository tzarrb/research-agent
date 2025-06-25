package com.ivan.researchagent.springai.llm.advisors;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description: 深度搜索的推理内容顾问
 * @author: ivan
 * @since: 2025/6/24/周二
 **/
@Slf4j
public class ReasoningContentAdvisor  implements BaseAdvisor {

    private final int order;

    public ReasoningContentAdvisor(Integer order) {
        this.order = order != null ? order : 0;
    }

    @Override
    public int getOrder() {

        return this.order;
    }

    @Override
    public ChatClientRequest before(final ChatClientRequest chatClientRequest, final AdvisorChain advisorChain) {
        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(final ChatClientResponse chatClientResponse, final AdvisorChain advisorChain) {
        ChatResponse resp = chatClientResponse.chatResponse();
        if (Objects.isNull(resp)) {
            return chatClientResponse;
        }

        log.debug("Advisor metadata output: {}", resp.getResults().get(0).getOutput().getMetadata());
        String reasoningContent = String.valueOf(resp.getResults().get(0).getOutput().getMetadata().get("reasoningContent"));
        if (StringUtils.isEmpty(reasoningContent)) {
            log.debug("No reasoning content found, skipping reasoning content processing.");
            return chatClientResponse;
        }

        log.debug("Advisor reasoning content: {}", reasoningContent);
        List<Generation> thinkGenerations = resp.getResults().stream()
                .map(generation -> {
                    AssistantMessage output = generation.getOutput();
                    AssistantMessage thinkAssistantMessage = new AssistantMessage(
                            String.format("<think>%s</think>", reasoningContent) + output.getText(),
                            output.getMetadata(),
                            output.getToolCalls(),
                            output.getMedia()
                    );
                    return new Generation(thinkAssistantMessage, generation.getMetadata());
                }).toList();

        ChatResponse thinkChatResp = ChatResponse.builder().from(resp).generations(thinkGenerations).build();
        return ChatClientResponse.builder().chatResponse(thinkChatResp).build();
    }
}
