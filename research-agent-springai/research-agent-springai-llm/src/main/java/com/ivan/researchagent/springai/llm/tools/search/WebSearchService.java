package com.ivan.researchagent.springai.llm.tools.search;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.ivan.researchagent.springai.llm.model.tool.WebSearchRequest;
import com.ivan.researchagent.springai.llm.model.tool.WebSearchResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/25/周三
 **/
public abstract class WebSearchService implements Function<WebSearchRequest, WebSearchResponse> {

    protected WebClient webClient;

    public abstract WebSearchResponse search(WebSearchRequest request);

}
