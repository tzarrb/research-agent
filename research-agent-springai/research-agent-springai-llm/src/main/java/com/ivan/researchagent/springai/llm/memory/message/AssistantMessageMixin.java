package com.ivan.researchagent.springai.llm.memory.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
public abstract class AssistantMessageMixin {

    @JsonCreator
    public AssistantMessageMixin(
            @JsonProperty("text") String textContent,
            @JsonProperty("metadata") Map<String, Object> metadata
    ) {
    }

//    @JsonCreator
//    public AssistantMessageMixin(
//            @JsonProperty("text") String textContent,
//            @JsonProperty("media") Collection<Media> media,
//            @JsonProperty("metadata") Map<String, Object> metadata,
//            @JsonProperty("toolCalls") Collection<AssistantMessage.ToolCall> toolCalls
//    ) {
//    }
}
