package com.ivan.researchagent.springai.llm.model.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/25/周三
 **/
@Data
public class WebSearchResponse {

    private String query;

    private String answer;

    private List<ImageInfo> images;

    private List<ResultInfo> results;

    @JsonProperty("response_time")
    private double responseTime;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageInfo {

        // 添加字符串参数的构造函数
        public ImageInfo(String url) {
            this.url = url;
        }

        private String url;

        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultInfo {
        private String title;

        private String content;

        private String url;

        private double score;

        @JsonProperty("raw_content")
        private String rawContent;
    }

}
