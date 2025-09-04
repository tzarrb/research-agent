package com.ivan.researchagent.springai.llm.model.tool;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ToolResponse(
        @JsonPropertyDescription("Tool工具执行结果输出") String output) {
}
