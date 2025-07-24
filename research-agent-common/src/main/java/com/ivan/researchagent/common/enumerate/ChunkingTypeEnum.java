package com.ivan.researchagent.common.enumerate;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/7/23/周三
 **/
public enum ChunkingTypeEnum {
    TOKEN_TEXT("tokenTextSplitter", "固定大小分块"),
    CONTENT_FORMAT("contentFormatTransformer", "基于内容结构分块"),
    KEYWORD_METADATA("keywordMetadataEnricher", "关键词元数据扩展"),
    SUMMARY_METADATA("summaryMetadataEnricher", "摘要元数据扩展"),
    ;

    private final String key;
    private final String desc;

    ChunkingTypeEnum(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }

    public static ChunkingTypeEnum getByKey(String key) {
        for (ChunkingTypeEnum value : values()) {
            if (value.getKey().equals(key)) {
                return value;
            }
        }
        return TOKEN_TEXT;
    }

}
