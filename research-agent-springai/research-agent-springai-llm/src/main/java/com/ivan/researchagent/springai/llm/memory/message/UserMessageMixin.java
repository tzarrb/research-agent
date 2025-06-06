package com.ivan.researchagent.springai.llm.memory.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.content.Media;


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
public abstract class UserMessageMixin {

    @JsonCreator
    public UserMessageMixin(
            @JsonProperty("text") String textContent,
            @JsonProperty("media") Collection<Media> media,
            @JsonProperty("metadata") Map<String, Object> metadata
    ) {
    }
}
