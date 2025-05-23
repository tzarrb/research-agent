package com.ivan.researchagent.plugin.coze.configuration;

import com.ivan.researchagent.plugin.coze.config.CozeProperties;
import com.ivan.researchagent.plugin.coze.function.TravelingPlannerFunction;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

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
@Configuration
@ConditionalOnBean(CozeProperties.class)
public class CozeAutoConfiguration {

    @Resource
    private CozeProperties cozeProperties;

    @Bean
    @ConditionalOnMissingBean
    @Description("获取旅行计划")
    public TravelingPlannerFunction getTravelingPlannerFunction() {
        return new TravelingPlannerFunction(cozeProperties);
    }
}
