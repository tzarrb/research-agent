package com.ivan.researchagent.springai.llm.tools.execute;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ivan.researchagent.springai.llm.model.tool.ToolExecuteResult;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/22/周五
 **/
@Slf4j
public class PythonExecuteTool implements BiFunction<String, ToolContext, ToolExecuteResult> {

    private UUID uuid = UUID.randomUUID();

    private Boolean arm64 = true;

    public static final String LLMMATH_PYTHON_CODE = "import sys; import math; import numpy as np; import numexpr as ne; input = '%s'; res = ne.evaluate(input); print(res)";

    private static final String name = "python_execute";

    public static final String description = """
			Executes Python code string. Note: Only print outputs are visible, function return values are not captured. Use print statements to see results.
			""";

    public static final String PARAMETERS = """
			{
			    "type": "object",
			    "properties": {
			        "code": {
			            "type": "string",
			            "description": "The Python code to execute."
			        }
			    },
			    "required": ["code"]
			}
			""";

    public static OpenAiApi.FunctionTool getToolDefinition() {
        OpenAiApi.FunctionTool.Function function = new OpenAiApi.FunctionTool.Function(description, name, PARAMETERS);
        OpenAiApi.FunctionTool functionTool = new OpenAiApi.FunctionTool(function);
        return functionTool;
    }

    public static FunctionToolCallback getFunctionToolCallback() {
        return FunctionToolCallback.builder(name, new PythonExecuteTool())
                .description(description)
                .inputSchema(PARAMETERS)
                .inputType(String.class)
                .build();
    }

    public ToolExecuteResult run(String toolInput) {
        log.info("PythonExecute toolInput:{}", toolInput);
        Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {
        });
        String code = (String) toolInputMap.get("code");

        // String result = PythonUtils.invokePythonCodeWithArch(code, arm64);
//        CodeExecutionResult codeExecutionResult = CodeUtils.executeCode(code, "python",
//                "tmp_" + uuid.toString() + ".py", arm64, new HashMap<>());
//        String result = codeExecutionResult.getLogs();

        return new ToolExecuteResult("result");
    }

    public Boolean getArm64() {
        return arm64;
    }

    public void setArm64(Boolean arm64) {
        this.arm64 = arm64;
    }

    @Override
    public ToolExecuteResult apply(@ToolParam(description = PARAMETERS) String s, ToolContext toolContext) {
        return run(s);
    }
}
