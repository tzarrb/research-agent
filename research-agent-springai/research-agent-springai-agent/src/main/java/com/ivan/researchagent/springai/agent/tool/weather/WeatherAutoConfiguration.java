package com.ivan.researchagent.springai.agent.tool.weather;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/14/周四
 **/
@Configuration
@ConditionalOnClass(WeatherService.class)
@ConditionalOnProperty(prefix = "spring.ai.alibaba.toolcalling.weather", name = "enabled", havingValue = "true")
public class WeatherAutoConfiguration {

    @Bean(name = "getWeatherFunction")
    @ConditionalOnMissingBean
    @Description("通过api接口获取城市未来几天的天气情况")
    public WeatherFunction getWeatherServiceFunction(WeatherService service) {
        return new WeatherFunction(service);
    }
}
