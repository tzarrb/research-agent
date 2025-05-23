package com.ivan.researchagent.common.enumerate;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/11/28 17:07
 **/
public enum LLMTypeEnum {
    OPENAI("openai", "openai"),
    CLAUDE("claude", "claude"),
    QIANFAN("qianfan", "百度千帆大模型平台"),
    DASHSCOPE("dashscope", "阿里百炼大模型平台"),
    ZHIPUAI("zhipuai", "智谱AI"),
    DOUBAO("doubao", "豆包大模型"),
    DEEPSEEK("deepseek", "DeepSeek"),
    OLLAMA("ollama", "ollama");

    private final String key;
    private final String desc;

    LLMTypeEnum(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }
}
