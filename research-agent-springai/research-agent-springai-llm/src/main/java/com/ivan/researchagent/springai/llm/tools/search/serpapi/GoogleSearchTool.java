package com.ivan.researchagent.springai.llm.tools.search.serpapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ivan.researchagent.springai.llm.model.tool.ToolExecuteResult;
import com.ivan.researchagent.springai.llm.model.tool.WebSearchRequest;
import com.ivan.researchagent.springai.llm.model.tool.WebSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
public class GoogleSearchTool implements BiFunction<String, ToolContext, ToolExecuteResult> {

    private SerpApieService service;

    private static final String name = "google_search";

    public static final String description = """
			Perform a Google search and return a list of relevant links.
			Use this tool when you need to find information on the web, get up-to-date data, or research specific topics.
			The tool returns a list of URLs that match the search query.
			""";

    public static final String PARAMETERS = """
			{
			    "type": "object",
			    "properties": {
			        "query": {
			            "type": "string",
			            "description": "(required) The search query to submit to Google."
			        },
			        "num_results": {
			            "type": "integer",
			            "description": "(optional) The number of search results to return. Default is 10.",
			            "default": 10
			        }
			    },
			    "required": ["query"]
			}
			""";

    private static final String SERP_API_KEY = System.getenv("spring.ai.alibaba.toolcalling.serpapisearch.api-key");

    public GoogleSearchTool() {
        service = new SerpApieService(new SerpApiProperties(SERP_API_KEY));
    }

    public GoogleSearchTool(SerpApiProperties properties) {
        service = new SerpApieService(properties);
    }

    public static OpenAiApi.FunctionTool getToolDefinition() {
        OpenAiApi.FunctionTool.Function function = new OpenAiApi.FunctionTool.Function(description, name, PARAMETERS);
        OpenAiApi.FunctionTool functionTool = new OpenAiApi.FunctionTool(function);
        return functionTool;
    }

    public static FunctionToolCallback getFunctionToolCallback() {
        return FunctionToolCallback.builder(name, new GoogleSearchTool())
                .description(description)
                .inputSchema(PARAMETERS)
                .inputType(String.class)
                .build();
    }

    public ToolExecuteResult run(String toolInput) {
        log.info("GoogleSearch toolInput:{}", toolInput);

        Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {
        });
        String query = (String) toolInputMap.get("query");

        Integer numResults = 2;
        if (toolInputMap.get("num_results") != null) {
            numResults = (Integer) toolInputMap.get("num_results");
        }
        WebSearchRequest request = new WebSearchRequest(query, numResults);
        WebSearchResponse response = service.search(request);
        String toret = "";
        if (Objects.isNull(response)) {
            toret = "No good search result found";
        } else {
            List<String> contents = response.getResults().stream().map(WebSearchResponse.ResultInfo::getContent).toList();
            toret = String.join("\n", contents);
        }

        log.warn("SerpapiTool result:{}", toret);
        return new ToolExecuteResult(toret);
    }

    @Override
    public ToolExecuteResult apply(@ToolParam(description = PARAMETERS) String s, ToolContext toolContext) {
        return run(s);
    }

}
