package com.ivan.researchagent.springai.agent.tool;

import com.ivan.researchagent.springai.agent.anno.ToolWarpper;
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
public class WeatherTools {

    @Tool(description = "通过城市获取天气情况")
    String getWeatherByCity(String city, ToolContext toolContext) {
        return city + "今天天气晴，温度15-25度，微风，适宜出行！";
    }

}
