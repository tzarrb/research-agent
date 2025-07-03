package com.ivan.researchagent.main.model.chat;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

import java.io.Serializable;

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
public class ChatInput implements Serializable {

    /**
     * 用户输入
     */
    private String userMessage;

    @JsonPropertyDescription("是否联网搜索")
    private Boolean enableWeb = false;

    @JsonPropertyDescription("是否支持本地知识库")
    private Boolean enableLocal = false;

}
