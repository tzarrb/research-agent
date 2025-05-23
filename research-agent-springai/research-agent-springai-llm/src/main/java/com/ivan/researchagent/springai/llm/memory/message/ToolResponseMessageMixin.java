package com.ivan.researchagent.springai.llm.memory.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.chat.messages.ToolResponseMessage;

import java.util.Collection;
import java.util.Map;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/4/18/周五
 **/
public abstract class ToolResponseMessageMixin {

    @JsonCreator
    public ToolResponseMessageMixin(
            @JsonProperty("text") String textContent,
            @JsonProperty("metadata") Map<String, Object> metadata,
            @JsonProperty("responses") Collection<ToolResponseMessage.ToolResponse> responses
    ) {
    }
}
