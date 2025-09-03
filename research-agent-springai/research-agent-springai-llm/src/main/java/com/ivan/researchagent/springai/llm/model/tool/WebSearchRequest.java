package com.ivan.researchagent.springai.llm.model.tool;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/22/周五
 **/
//@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonClassDescription("web search request")
public record WebSearchRequest(
        @JsonProperty(required = true, value = "query")
        @JsonPropertyDescription("The query " + "keyword e.g. Alibaba")
        String query,
        @JsonProperty(required = false, value = "limit", defaultValue = "5")
        @JsonPropertyDescription("Maximum number of results to return")
        Integer limit) {
}
