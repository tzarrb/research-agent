package com.ivan.researchagent.springai.llm.tools.file;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ivan.researchagent.springai.llm.model.tool.ToolExecuteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

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
public class FileSaverTool  implements Function<String, ToolExecuteResult> {

    private static final String name = "file_saver";

    private static final String description = """
			Save content to a local file at a specified path.
			Use this tool when you need to save text, code, or generated content to a file on the local filesystem.
			The tool accepts content and a file path, and saves the content to that location.""";

    private static final String PARAMETERS = """
			{
			    "type": "object",
			    "properties": {
			        "content": {
			            "description": "(required) The content to save to the file.",
			            "type": "string"
			        },
			        "file_path": {
			            "description": "(required) The path where the file should be saved, including filename and extension.",
			            "type": "string"
			        }
			    },
			    "required": ["content", "file_path"],
			    "additionalProperties": false
			}
			""";

    private static final FileSaverTool INSTANCE = new FileSaverTool();

    public static OpenAiApi.FunctionTool getToolDefinition() {
        OpenAiApi.FunctionTool.Function function = new OpenAiApi.FunctionTool.Function(description, name, PARAMETERS);
        OpenAiApi.FunctionTool functionTool = new OpenAiApi.FunctionTool(function);
        return functionTool;
    }

    public static FunctionToolCallback getFunctionToolCallback() {
        return FunctionToolCallback.builder(name, INSTANCE)
                .description(description)
                .inputSchema(PARAMETERS)
                .inputType(String.class)
                .build();
    }

    public ToolExecuteResult run(String toolInput) {
        log.info("FileSaver toolInput:{}", toolInput);
        try {
            Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {
            });
            String content = (String) toolInputMap.get("content");
            String filePath = (String) toolInputMap.get("file_path");
            File file = new File(filePath);
            File directory = file.getParentFile();
            if (directory != null && !directory.exists()) {
                directory.mkdirs();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write(content);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

            return new ToolExecuteResult("Content successfully saved to " + filePath);
        }
        catch (Throwable e) {
            return new ToolExecuteResult("Error saving file: " + e.getMessage());
        }
    }

    @Override
    public ToolExecuteResult apply(@ToolParam(description = PARAMETERS) String s) {
        return run(s);
    }

}
