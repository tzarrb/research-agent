package com.ivan.researchagent.springai.agent.graph.agent.travel;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.google.common.collect.Lists;
import com.ivan.researchagent.springai.agent.graph.core.GraphUtil;
import com.ivan.researchagent.springai.agent.tool.CommonTools;
import com.ivan.researchagent.springai.llm.model.chat.ChatParams;
import com.ivan.researchagent.springai.llm.service.ChatService;

import jakarta.annotation.Resource;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * ReAct推理执行
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/14/周四
 **/
@Configuration
public class TravelAutoconfiguration {

    @Resource
    private CommonTools commonTools;

    @Resource
    ToolCallbackProvider commonToolCallbackProvider;
    @Resource
    ToolCallbackProvider asyncMcpToolCallbackProvider;

    @Bean
    public ReactAgent travelAgent(ChatService chatService, ToolCallbackResolver resolver) throws GraphStateException {
        ChatParams chatParams = ChatParams.builder()
                .enableMemory(true)
                .enableStream(false)
                .defaultToolNames(Lists.newArrayList("getWeatherService"))
                .build();
        ChatClient chatClient= chatService.getChatClient(chatParams);

        return ReactAgent.builder()
                .name("Travel Plan Agent")
                .chatClient(chatClient)
                .resolver(resolver)
                .maxIterations(10)
                .build();
    }

    @Bean
    public CompiledGraph travelGraph(@Qualifier("travelAgent") ReactAgent reactAgent) throws GraphStateException {
        GraphRepresentation graphRepresentation = reactAgent.getStateGraph().getGraph(GraphRepresentation.Type.PLANTUML);

        System.out.println("\n\n");
        System.out.println(graphRepresentation.content());
        System.out.println("\n\n");

        CompileConfig compileConfig = GraphUtil.getCompileConfig();
        return reactAgent.getAndCompileGraph(compileConfig);
    }

}
