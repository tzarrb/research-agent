package com.ivan.researchagent.plugin.coze.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/12 17:58
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "coze.work-flow")
@ConditionalOnProperty(value = "coze.access-token")
public class CozeProperties {

    @Value("${coze.access-token}")
    private String accessToken;

    @Value("${coze.work-flow.base-url}")
    private String workFlowBaseUrl;

    @Value("${coze.work-flow.traveling-planner}")
    private String travelingPlanner;

//    @Value("${coze.work-flow.sources}")
//    private Map<String, String> workFlowSources;
}
