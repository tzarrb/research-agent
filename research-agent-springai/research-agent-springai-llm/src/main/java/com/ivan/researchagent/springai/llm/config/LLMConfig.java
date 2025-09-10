package com.ivan.researchagent.springai.llm.config;

import com.ivan.researchagent.common.constant.Constant;
import lombok.Data;
import org.glassfish.jaxb.runtime.v2.runtime.reflect.opt.Const;
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
    public static final String PREFIX = Constant.PREFIX + ".llm";

    @Value("${research.agent.llm.provider:dashscope}")
    private String defaultProvider;

    //@Value("${research.agent.llm.model:qwen-max}")
    @Value("${spring.ai.dashscope.chat.options.model:qwen-max}")
    private String defaultModel;

    @Value("${spring.ai.dashscope.multi.options.model:qwen-vl-max-latest}")
    private String defaultMultiModel;

    @Value("${research.agent.llm.think.model:deepseek-r1}")
    private String defaultThinkModel;

}
