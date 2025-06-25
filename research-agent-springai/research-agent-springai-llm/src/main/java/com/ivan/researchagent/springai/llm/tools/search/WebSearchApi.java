package com.ivan.researchagent.springai.llm.tools.search;

import com.ivan.researchagent.springai.llm.model.tool.WebSearchResponse;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/25/周三
 **/
public abstract class WebSearchApi {

    protected WebClient webClient;

    public abstract WebSearchResponse search(String query);
}
