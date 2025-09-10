package com.ivan.researchagent.springai.llm.model.image;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/9/9/周二
 **/
@Data
public class ImageRequest {

    @JsonPropertyDescription("图像生成提示词")
    private String prompt;

    @JsonPropertyDescription("图像生成分标率，1024*680")
    private String resolution;

    @JsonPropertyDescription("图像生成风格，如：摄影写实")
    private String style;

    @JsonPropertyDescription("图像生成数量")
    private Integer number = 1;
}
