package com.ivan.researchagent.springai.llm.model.rag;

import com.ivan.researchagent.common.enumerate.ChunkingTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

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
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
     * 存储资源
     */
    private Resource resource;

    /**
     * 元数据
     */
    private Map<String, Object> metadata;

    /**
     * 分块类型
     */
    private ChunkingTypeEnum chunkingType;
}
