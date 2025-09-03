package com.ivan.researchagent.springai.agent.graph.agent.customer;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.constant.SaverConstant;
import com.alibaba.cloud.ai.graph.checkpoint.savers.FileSystemSaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.node.QuestionClassifierNode;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.ivan.researchagent.springai.agent.graph.agent.customer.node.RecordingNode;
import com.ivan.researchagent.springai.agent.graph.agent.doctor.node.HumanFeedbackNode;
import com.ivan.researchagent.springai.agent.graph.core.GraphSerializer;
import com.ivan.researchagent.springai.agent.graph.core.GraphUtil;
import com.ivan.researchagent.springai.llm.model.chat.ChatRequest;
import com.ivan.researchagent.springai.llm.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/9/1/周一
 **/
@Configuration
public class CustomerFeedbackAutoConfiguration {
    
    @Bean
    public CompiledGraph customerFeedbackGraph(ChatService chatService) throws GraphStateException {
        ChatRequest chatRequest = ChatRequest.builder()
                .enableMemory(true)
                .enableStream(false)
                .build();
        ChatClient chatClient = chatService.getchatClient(chatRequest);

        // 评价分类器 - 区分正面/负面评价
        QuestionClassifierNode feedbackClassifier = QuestionClassifierNode.builder()
                .chatClient(chatClient)
                .inputTextKey("input")
                .outputKey("classifier_output")
                .categories(List.of("positive feedback", "negative feedback"))
                .build();

        // 问题细分器 - 对负面评价进行细分
        QuestionClassifierNode specificQuestionClassifier = QuestionClassifierNode.builder()
                .chatClient(chatClient)
                .inputTextKey("input")
                .outputKey("classifier_output")
                .categories(List.of("after-sale service", "transportation", "product quality", "others"))
                .build();

        // 路由决策器
        EdgeAction classifierRoutingEdge = new EdgeAction() {
            @Override
            public String apply(OverAllState state) {
                return state.value("classifier_output", StateGraph.END);
            }
        };

        // 状态工厂定义 - 简化的状态管理
        KeyStrategyFactory stateFactory = () -> {
            Map<String, KeyStrategy> strategies = new HashMap<>();
            strategies.put("input", new ReplaceStrategy());
            strategies.put("classifier_output", new ReplaceStrategy());
            strategies.put("solution", new ReplaceStrategy());
            return strategies;
        };
        // 构建工作流 - 声明式API
        StateGraph stateGraph = new StateGraph("客户服务评价处理", stateFactory)
                .addNode("feedback_classifier", node_async(feedbackClassifier))
                .addNode("specific_question_classifier", node_async(specificQuestionClassifier))
                .addNode("recorder", node_async(new RecordingNode()))

                .addEdge(START, "feedback_classifier")
                .addConditionalEdges("feedback_classifier",
                        edge_async(classifierRoutingEdge),
                        Map.of("positive", "recorder", "negative", "specific_question_classifier"))
                .addConditionalEdges("specific_question_classifier",
                        edge_async(classifierRoutingEdge),
                        Map.of("after-sale service", "recorder", "transportation", "recorder",
                                "product quality", "recorder", "others", "recorder"))
                .addEdge("recorder", END);

        CompileConfig compileConfig = GraphUtil.getCompileConfig();
        CompiledGraph compiledGraph = stateGraph.compile(compileConfig);
        compiledGraph.setMaxIterations(10);
        return compiledGraph;
    }


    public String classifierRouting(OverAllState state) {
        String nextStep = state.value("classifier_output", StateGraph.END);

        return nextStep;
    }

}
