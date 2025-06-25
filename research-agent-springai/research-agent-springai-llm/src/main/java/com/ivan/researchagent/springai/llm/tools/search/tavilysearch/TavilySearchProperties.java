package com.ivan.researchagent.springai.llm.tools.search.tavilysearch;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description: spring.ai.tavily
 * @author: ivan
 * @since: 2025/6/25/周三
 **/
@Setter
@Getter
@ConfigurationProperties(prefix = TavilySearchProperties.TAVILY_SEARCH_PREFIX)
public class TavilySearchProperties {

    protected static final String TAVILY_SEARCH_PREFIX = "spring.ai.alibaba.toolcalling.tavilysearch";

    private String baseUrl;

    private String apiKey;

    private String topic = "general";

    private String searchDepth = "basic";

    private int maxResults = 5;

    private int chunksPerSource = 3;

    private int days = 7;

    private boolean includeRawContent = false;

    private boolean includeImages = false;

    private boolean includeImageDescriptions = false;

    private boolean includeAnswer = false;

}
