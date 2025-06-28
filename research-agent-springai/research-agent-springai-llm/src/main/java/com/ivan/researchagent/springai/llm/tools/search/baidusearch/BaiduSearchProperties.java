package com.ivan.researchagent.springai.llm.tools.search.baidusearch;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
@ConfigurationProperties(prefix = BaiduSearchProperties.BAIDU_SEARCH_PREFIX)
public class BaiduSearchProperties {

    protected static final String BAIDU_SEARCH_PREFIX = "spring.ai.alibaba.toolcalling.baidusearch";

    private String baseUrl;

    private Integer maxResults = 5;

}
