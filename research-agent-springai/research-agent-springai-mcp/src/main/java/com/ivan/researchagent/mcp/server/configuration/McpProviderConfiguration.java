package com.ivan.researchagent.mcp.server.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivan.researchagent.mcp.server.service.OpenMeteoService;
import com.ivan.researchagent.mcp.server.service.TimeService;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/7/2/周三
 **/
@Configuration
public class McpProviderConfiguration {

    @Bean
    public ToolCallbackProvider weatherTools(OpenMeteoService openMeteoService) {
        return MethodToolCallbackProvider.builder().toolObjects(openMeteoService).build();
    }

    @Bean
    public ToolCallbackProvider timeTools(TimeService timeService) {
        return MethodToolCallbackProvider.builder().toolObjects(timeService).build();
    }

    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> systemResources() {
        // 定义资源
        var systemInfoResource = new McpSchema.Resource(
                "system-info", "系统信息资源", "获取当前系统状态信息", null, null);

        // 创建资源规范
        var resourceSpec = new McpServerFeatures.SyncResourceSpecification(
                systemInfoResource,
                (exchange, request) -> {
                    // 获取系统信息
                    Map<String, Object> systemInfo =new HashMap<>();
                    systemInfo.put("cpu", Runtime.getRuntime().availableProcessors());
                    systemInfo.put("memory", Runtime.getRuntime().totalMemory());
                    systemInfo.put("freeMemory", Runtime.getRuntime().freeMemory());
                    systemInfo.put("timestamp", System.currentTimeMillis());

                    // 转换为JSON
                    String jsonContent= null;
                    try {
                        jsonContent = new ObjectMapper().writeValueAsString(systemInfo);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    // 返回资源内容
                    return new McpSchema.ReadResourceResult(
                             List.of(new McpSchema.TextResourceContents(
                                     request.uri(), "application/json", jsonContent)));
                }
         );

        return List.of(resourceSpec);
    }
}
