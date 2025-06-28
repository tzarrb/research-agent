package com.ivan.researchagent.springai.agent.model.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @author: ivan
 * @since: 2025/1/4 23:45
 */
public record FuncRequest(
        @JsonProperty(required = true) @JsonPropertyDescription(value = "用户原始的输入指令") String input) {
}
