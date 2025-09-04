package com.ivan.researchagent.springai.llm.tools.core;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.support.ToolUtils;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/22/周五
 **/
public abstract class BaseToolCallback implements ToolCallback {

    private final String INPUT_SCHEMA = """
                    {
                        "type": "string",
                        "required": true,
			            "description": "The input to submit to Tool."
                    }
                """;

    private String name;

    private String description;

    private String inputSchema;

    private Type inputType;

    public BaseToolCallback(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public BaseToolCallback(String name, String description, String inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }

    public BaseToolCallback(String name, String description, String inputSchema, Type inputType) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
        this.inputType = inputType;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        var toolDefinition = DefaultToolDefinition.builder()
                .name(this.name)
                .description(StringUtils.hasText(this.description) ? this.description
                        : ToolUtils.getToolDescriptionFromName(this.name))
                .inputSchema(StringUtils.hasText(this.inputSchema) ? this.inputSchema
                        : (Objects.nonNull(this.inputType) ? JsonSchemaGenerator.generateForType(this.inputType)
                            : INPUT_SCHEMA))
                .build();
        return toolDefinition;
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return ToolMetadata.builder().returnDirect(true).build();
    }

    @Override
    public String call(String toolInput) {
        return "";
    }

}
