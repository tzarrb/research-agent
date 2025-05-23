package com.ivan.researchagent.main.model.vo;

import lombok.Data;

import java.util.Map;

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
public class VectorStoreData {

    /**
     * 唯一标识
     */
    private String id;

    /**
     * 存储内容
     */
    private String content;

    /**
     * 元数据
     */
    private Map<String, Object> metadata;
}
