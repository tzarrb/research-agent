package com.ivan.researchagent.springai.agent.graph.workflow;

import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.OverAllStateFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.node.LlmNode;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.ivan.researchagent.springai.llm.model.chat.ChatRequest;
import com.ivan.researchagent.springai.llm.service.ChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * 链式工作流
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/12/周二
 **/
@Slf4j
@Configuration
public class ChainGraphConfiguration {

    @Resource
    private ChatService chatService;

    @Bean
    public StateGraph documentProcessingChain() throws GraphStateException {
        ChatRequest chatRequest = ChatRequest.builder().build();
        ChatClient chatClient= chatService.getchatClient(chatRequest);

        // 定义全局状态
        OverAllStateFactory stateFactory= () -> {
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy("input", new ReplaceStrategy());
            state.registerKeyAndStrategy("outline", new ReplaceStrategy());
            state.registerKeyAndStrategy("content", new ReplaceStrategy());
            state.registerKeyAndStrategy("final_document", new ReplaceStrategy());
            return state;
        };

        // 创建处理节点 - 使用LlmNode
        LlmNode outlineNode= LlmNode.builder()
                .chatClient(chatClient)
                .systemPromptTemplate("你是一个专业的文档架构师，擅长根据需求生成清晰的文档大纲。")
                .userPromptTemplate("基于以下需求生成详细的文档大纲：{input}")
                .paramsKey("input_params")
                .outputKey("outline")
                .build();

        LlmNode contentNode= LlmNode.builder()
                .chatClient(chatClient)
                .systemPromptTemplate("你是一个专业的技术写作专家，能够根据大纲编写高质量的技术文档。")
                .userPromptTemplate("基于以下大纲编写详细的技术文档内容：{outline}")
                .paramsKey("content_params")
                .outputKey("content")
                .build();

        LlmNode reviewNode= LlmNode.builder()
                .chatClient(chatClient)
                .systemPromptTemplate("你是一个资深的文档审核专家，能够发现问题并提供改进建议。")
                .userPromptTemplate("请审查并优化以下文档内容，确保逻辑清晰、表达准确：{content}")
                .paramsKey("review_params")
                .outputKey("final_document")
                .build();

        // 构建链式工作流
        StateGraph stateGraph = new StateGraph("文档生成链式工作流", stateFactory)
                .addNode("outline_generator", node_async(outlineNode))
                .addNode("content_writer", node_async(contentNode))
                .addNode("content_reviewer", node_async(reviewNode))

                .addEdge(StateGraph.START, "outline_generator")
                .addEdge("outline_generator", "content_writer")
                .addEdge("content_writer", "content_reviewer")
                .addEdge("content_reviewer", StateGraph.END);

        // 添加 PlantUML 打印
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML,
                "expander flow");
        log.info("\n=== expander UML Flow ===");
        log.info(representation.content());
        log.info("==================================\n");

        return stateGraph;
    }
}
