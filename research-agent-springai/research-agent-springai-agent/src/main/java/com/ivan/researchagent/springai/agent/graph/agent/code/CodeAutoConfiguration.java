package com.ivan.researchagent.springai.agent.graph.agent.code;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.agent.ReflectAgent;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.constant.SaverConstant;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.ivan.researchagent.springai.agent.graph.agent.code.node.CodeGeneratorNode;
import com.ivan.researchagent.springai.agent.graph.agent.code.node.CodeReviewerNode;
import com.ivan.researchagent.springai.llm.model.chat.ChatRequest;
import com.ivan.researchagent.springai.llm.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * Relect自我反思智能体
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/14/周四
 **/
@Configuration
public class CodeAutoConfiguration {
    @Bean
    public CompiledGraph codeGraph(ChatService chatService) throws GraphStateException {

        ChatRequest chatRequest = ChatRequest.builder()
                .enableMemory( true)
                .enableStream(false)
                .build();
        ChatClient chatClient= chatService.getchatClient(chatRequest);

        ReflectAgent reflectAgent = ReflectAgent.builder()
                .graph(new CodeGeneratorNode(chatClient))
                .reflection(new CodeReviewerNode(chatClient))
                .maxIterations(2)
                .build();

        SaverConfig saverConfig = SaverConfig.builder().register(SaverConstant.MEMORY, new MemorySaver()).build();
        CompileConfig compileConfig = CompileConfig.builder()
                .saverConfig(saverConfig)
                .build();
        return reflectAgent.getAndCompileGraph(compileConfig);
    }
}
