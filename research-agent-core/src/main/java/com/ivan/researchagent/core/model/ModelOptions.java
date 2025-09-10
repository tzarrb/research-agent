package com.ivan.researchagent.core.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Builder;
import lombok.Data;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.util.List;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/09 13:40
 **/
@Data
@Builder
//@NoArgsConstructor
//@AllArgsConstructor
public class ModelOptions {

    @JsonPropertyDescription("大模型平台")
    private String provider;

    @JsonPropertyDescription("大模型名称")
    private String model;

    @JsonPropertyDescription("对话输入信息类型，如：TEXT,IMAGE,VIDEO")
    private String messageType = "TEXT";

    @JsonPropertyDescription("对话记忆的唯一标识")
    private String conversantId;

    @JsonPropertyDescription("是否流式对话方法")
    private Boolean enableStream = false;

    @JsonPropertyDescription("是否使用对话记忆")
    private Boolean enableMemory = true;

    @JsonPropertyDescription("是否支持多模态")
    private Boolean enableMulti = false;

    @JsonPropertyDescription("是否启用日志记录")
    private Boolean enableLogging = true;

    @JsonPropertyDescription("是否联网搜索")
    private Boolean enableSearch = true;

    @JsonPropertyDescription("输出格式")
    private String formatType;

    @JsonPropertyDescription("系统提示词")
    private String defaultSystem;

    @JsonPropertyDescription("用户提示词")
    private String defaultUser;

    @JsonPropertyDescription("大模型调用工具")
    private List<Object> defaultTools;

    @JsonPropertyDescription("大模型调用工具名称")
    private List<String> defaultToolNames;

    @JsonPropertyDescription("大模型调用工具回调")
    private List<ToolCallback> defaultToolCallbacks;

    @JsonPropertyDescription("大模型调用工具回调提供者")
    private List<ToolCallbackProvider> defaultToolCallbackProviders;

}
