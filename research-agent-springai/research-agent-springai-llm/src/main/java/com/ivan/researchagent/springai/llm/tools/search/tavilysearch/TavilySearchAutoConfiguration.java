package com.ivan.researchagent.springai.llm.tools.search.tavilysearch;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/25/周三
 **/
@Configuration
@EnableConfigurationProperties(TavilySearchProperties.class)
@ConditionalOnProperty(prefix = TavilySearchProperties.TAVILY_SEARCH_PREFIX, name = "enabled", havingValue = "true")
public class TavilySearchAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TavilySearchApi tavilySearchApi(TavilySearchProperties properties) {
        return new TavilySearchApi(properties);
    }

}
