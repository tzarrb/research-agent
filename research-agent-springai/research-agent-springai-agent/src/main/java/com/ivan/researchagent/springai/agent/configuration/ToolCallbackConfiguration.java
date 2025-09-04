package com.ivan.researchagent.springai.agent.configuration;

import com.ivan.researchagent.springai.agent.tool.CommonTools;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/9/4/周四
 **/
@Configuration
public class ToolCallbackConfiguration {

    @Bean
    public MethodToolCallbackProvider commonToolCallbackProvider(CommonTools commonTools) {
        return MethodToolCallbackProvider.builder().toolObjects(commonTools).build();
    }

//    @Bean
//    public SyncMcpToolCallbackProvider syncMcpToolCallbackProvider(List<McpSyncClient> mcpSyncClients) {
//        return new SyncMcpToolCallbackProvider(mcpSyncClients);
//    }

    @Bean
    public AsyncMcpToolCallbackProvider asyncMcpToolCallbackProvider(List<McpAsyncClient> mcpAsyncClients) {
        return new AsyncMcpToolCallbackProvider(mcpAsyncClients);
    }

}
