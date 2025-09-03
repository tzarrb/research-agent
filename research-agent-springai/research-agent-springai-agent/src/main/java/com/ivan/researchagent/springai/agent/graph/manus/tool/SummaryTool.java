package com.ivan.researchagent.springai.agent.graph.manus.tool;

import com.ivan.researchagent.springai.llm.model.tool.ToolExecuteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;

import java.util.function.BiFunction;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * 总结工具
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/21/周四
 **/
@Slf4j
public class SummaryTool implements BiFunction<String, ToolContext, ToolExecuteResult> {

    private static final String name = "summary";

    private static final String description = "Record the summary of current step.";

    private static final String PARAMETERS = """
			{
			  "type" : "object",
			  "properties" : {
			    "summary" : {
			      "type" : "string",
			      "description" : "The output of current step, better make a summary."
			    }
			  },
			  "required" : [ "summary" ]
			}
			""";

    private String conversationId;

    public SummaryTool(String conversationId) {
        this.conversationId = conversationId;
    }

    public static OpenAiApi.FunctionTool getToolDefinition() {
        OpenAiApi.FunctionTool.Function function = new OpenAiApi.FunctionTool.Function(description, name, PARAMETERS);
        OpenAiApi.FunctionTool functionTool = new OpenAiApi.FunctionTool(function);
        return functionTool;
    }

    public static FunctionToolCallback getFunctionToolCallback(String conversationId) {
        return FunctionToolCallback.builder(name, new SummaryTool(conversationId))
                .description(description)
                .inputSchema(PARAMETERS)
                .inputType(String.class)
                .toolMetadata(ToolMetadata.builder().returnDirect(true).build())
                .build();
    }

    public ToolExecuteResult run(String toolInput) {
        log.info("Summary toolInput:{}", toolInput);
        return new ToolExecuteResult(toolInput);
    }

    @Override
    public ToolExecuteResult apply(@ToolParam(description = PARAMETERS) String s, ToolContext toolContext) {
        // chatMemory.add(conversationId, toolContext.getToolCallHistory());
        return run(s);
    }

}
