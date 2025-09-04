package com.ivan.researchagent.springai.llm.tools.search.baidu;

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
 * @since: 2025/6/25/周三
 **/
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = BaiduSearchProperties.BAIDU_SEARCH_PREFIX)
public class BaiduSearchProperties {

    protected static final String BAIDU_SEARCH_PREFIX = "spring.ai.alibaba.toolcalling.baidu.search";

    private String baseUrl;

    private Integer maxResults = 5;

}
