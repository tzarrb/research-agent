package com.ivan.researchagent.springai.agent.graph.workflow;

import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.OverAllStateFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.node.LlmNode;
import com.alibaba.cloud.ai.graph.node.QuestionClassifierNode;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.ivan.researchagent.springai.llm.model.chat.ChatRequest;
import com.ivan.researchagent.springai.llm.service.ChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * 路由工作流-客服服务
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/12/周二
 **/
@Slf4j
@Configuration
public class RoutingGraphConfiguration {

    @Resource
    private ChatService chatService;

    @Bean
    public StateGraph customerServiceRouting() throws GraphStateException {
        ChatRequest chatRequest = ChatRequest.builder().build();
        ChatClient chatClient= chatService.getchatClient(chatRequest);

        // 定义全局状态
        OverAllStateFactory stateFactory= () -> {
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy("input", new ReplaceStrategy());
            state.registerKeyAndStrategy("classifier_output", new ReplaceStrategy());
            state.registerKeyAndStrategy("response", new ReplaceStrategy());
            return state;
        };

        // 问题分类节点
        QuestionClassifierNode classifier= QuestionClassifierNode.builder()
                .chatClient(chatClient)
                .inputTextKey("input")
                .outputKey("classifier_output")
                .categories(List.of("billing", "technical", "general"))
                .classificationInstructions(List.of(
                        "分析客户问题类型：账单问题、技术支持还是一般咨询"))
                .build();

        // 专业处理节点 - 使用LlmNode
        LlmNode billingNode= LlmNode.builder()
                .chatClient(chatClient)
                .systemPromptTemplate("你是一位专业的账单专家，具有丰富的财务和账单处理经验。请以专业、耐心的态度帮助客户解决账单相关问题。")
                .userPromptTemplate("客户账单问题：{input}\n请提供详细的解决方案和操作步骤。")
                .paramsKey("billing_params")
                .outputKey("response")
                .build();

        LlmNode technicalNode= LlmNode.builder()
                .chatClient(chatClient)
                .systemPromptTemplate("你是一位资深的技术支持工程师，擅长诊断和解决各种技术问题。请提供清晰的技术解决方案。")
                .userPromptTemplate("技术问题描述：{input}\n请分析问题原因并提供解决步骤。")
                .paramsKey("technical_params")
                .outputKey("response")
                .build();

        LlmNode generalNode= LlmNode.builder()
                .chatClient(chatClient)
                .systemPromptTemplate("你是一位友善的客服代表，致力于为客户提供优质的服务体验。请以温暖、专业的态度回应客户咨询。")
                .userPromptTemplate("客户咨询：{input}\n请提供有帮助的回复和建议。")
                .paramsKey("general_params")
                .outputKey("response")
                .build();

        // 路由决策器
        EdgeAction routingEdge = new EdgeAction() {
            @Override
            public String apply(OverAllState state) throws Exception {
                return state.value("classifier_output", StateGraph.END);
            }
        };

        // 构建路由工作流
        StateGraph stateGraph = new StateGraph("客服路由工作流", stateFactory)
                .addNode("classifier", AsyncNodeAction.node_async(classifier))
                .addNode("billing_handler", AsyncNodeAction.node_async(billingNode))
                .addNode("technical_handler", AsyncNodeAction.node_async(technicalNode))
                .addNode("general_handler", AsyncNodeAction.node_async(generalNode))

                .addEdge(StateGraph.START, "classifier")
                .addConditionalEdges("classifier",
                        AsyncEdgeAction.edge_async(routingEdge),
                        Map.of("billing", "billing_handler",
                                "technical", "technical_handler",
                                "general", "general_handler"))
                .addEdge("billing_handler", StateGraph.END)
                .addEdge("technical_handler", StateGraph.END)
                .addEdge("general_handler", StateGraph.END);

        // 添加 PlantUML 打印
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML,
                "expander flow");
        log.info("\n=== expander UML Flow ===");
        log.info(representation.content());
        log.info("==================================\n");

        return stateGraph;
    }
}
