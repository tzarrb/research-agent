package com.ivan.researchagent.main.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/9/周一
 **/
@Data
@JsonPropertyOrder({"title", "date", "author", "content"}) // 指定属性的顺序
public class ArticleVO {

    @JsonPropertyDescription("文章标题")
    private String title;

    @JsonPropertyDescription("文章作者")
    private String author;

    @JsonPropertyDescription("文章日期")
    private String date;

    @JsonPropertyDescription("文章内容")
    private String content;

}
