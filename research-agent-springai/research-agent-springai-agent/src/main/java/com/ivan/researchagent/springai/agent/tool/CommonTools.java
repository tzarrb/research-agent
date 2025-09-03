package com.ivan.researchagent.springai.agent.tool;

import com.alibaba.cloud.ai.toolcalling.weather.WeatherService;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.ivan.researchagent.springai.agent.anno.ToolWarpper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/4/7/周一
 **/
@Slf4j
@ToolWarpper
public class CommonTools {
    @Resource
    WeatherService weatherService;

    @Tool(name = "getWeatherByCity", description = "通过城市获取天气情况")
    String getWeatherByCity(@JsonPropertyDescription("城市, 比如杭州") String city,
                            @JsonPropertyDescription("天数, 比如3") Integer days,
                            ToolContext toolContext) {
        //return city + "今天天气晴，温度15-25度，微风，适宜出行！";
        WeatherService.Request request = new WeatherService.Request(city, days);
        return weatherService.apply(request).toString();
    }

}
