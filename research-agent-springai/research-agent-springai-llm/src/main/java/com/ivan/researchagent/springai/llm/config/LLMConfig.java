package com.ivan.researchagent.springai.llm.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/5/14/周三
 **/
@Data
@Configuration
public class LLMConfig {

    @Value("${llm.ai.provider:dashscope}")
    private String defaultProvider;

    @Value("${llm.ai.model:qwen-max}")
    private String defaultModel;

}
