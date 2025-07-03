package com.ivan.researchagent.springai.agent.agentic.tool;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.ivan.researchagent.springai.agent.anno.ToolAgent;
import com.ivan.researchagent.common.constant.Constant;
import com.ivan.researchagent.springai.llm.model.chat.ChatRequest;
import com.ivan.researchagent.springai.llm.model.chat.ChatResult;
import com.ivan.researchagent.springai.llm.service.ChatService;
import io.modelcontextprotocol.client.McpAsyncClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.List;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/4/7/周一
 **/
@Slf4j
@ToolAgent
public class OtherToolAgent extends AbstractToolAgent {

    @Resource
    private ChatService chatService;

//    @Resource
//    private List<McpSyncClient> mcpSyncClients;  // For sync client
    @Resource
    private List<McpAsyncClient> mcpAsyncClients;  // For async client

//    @Resource
//    private List<SyncMcpToolCallbackProvider> toolCallbackProviders;

    @Override
    public ToolDefinition getToolDefinition() {
        ToolDefinition toolDefinition = ToolDefinition.builder()
                .name("OtherToolAgent")
                .description("未明确分类的智能体")
                .inputSchema("""
                    {
                        "type": "string",
                        "required": true
                    }
                """)
                .build();
        return toolDefinition;
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        String conversantId = (String)toolContext.getContext().get(Constant.CONVERSANT_ID);
        String originalInput = (String)toolContext.getContext().get(Constant.ORIGINAL_INPUT);
        ChatRequest chatRequest = JSON.parseObject(JSON.toJSONString(toolContext.getContext().get(Constant.CHAT_MESSAGE)), ChatRequest.class);

        chatRequest.setEnableAgent(false);
        chatRequest.setUserMessage(originalInput);
        chatRequest.setSystemMessage("");
        chatRequest.setToolCallBacks(null);
        //chatMessage.setToolCallbackProviders(toolCallbackProviders);
        AsyncMcpToolCallbackProvider toolCallbackProvider = new AsyncMcpToolCallbackProvider(mcpAsyncClients);
        //SyncMcpToolCallbackProvider toolCallbackProvider = new SyncMcpToolCallbackProvider(mcpSyncClients);
        chatRequest.setToolCallbackProviders(Lists.newArrayList(toolCallbackProvider));

        ToolCallback[] toolCallbacks = toolCallbackProvider.getToolCallbacks();

        ChatResult chatResult = chatService.chat(chatRequest);

        log.info("sessionId:{}, otherToolAgent request:{}, response: {}", conversantId, JSON.toJSONString(originalInput), chatResult.getContent());
        return chatResult.getContent();
    }
}
