package com.ivan.researchagent.springai.llm.tools.search.baidu;

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
@EnableConfigurationProperties(BaiduSearchProperties.class)
@ConditionalOnProperty(prefix = BaiduSearchProperties.BAIDU_SEARCH_PREFIX, name = "enabled", havingValue = "true")
public class BaiduSearchAutoConfiguration {

    @Bean(name = "baiduSearchService")
    @ConditionalOnMissingBean
    public BaiduSearchService baiduSearchService(BaiduSearchProperties properties) {
        return new BaiduSearchService(properties);
    }

}
