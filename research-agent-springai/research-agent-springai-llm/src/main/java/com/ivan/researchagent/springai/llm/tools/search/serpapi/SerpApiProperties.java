package com.ivan.researchagent.springai.llm.tools.search.serpapi;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/21/周四
 **/
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = SerpApiProperties.SERPAPI_SEARCH_PREFIX)
public class SerpApiProperties {

    protected static final String SERPAPI_SEARCH_PREFIX = "spring.ai.alibaba.toolcalling.serpapi";

    public static final String SERP_ENGINE = "google"; // google, bing, yahoo, baidu, amazon, duckduckgo

    public static final String SERP_API_URL = "https://serpapi.com/search";

    public static final String USER_AGENT_VALUE = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    public SerpApiProperties() {

    }

    public SerpApiProperties(String apikey) {
        this.apikey = apikey;
        this.baseUrl = SERP_API_URL;
        this.engine = SERP_ENGINE;
    }


    private String baseUrl;

    private String apikey;

    private String engine;

}
