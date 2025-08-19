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
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.ivan.researchagent.springai.llm.model.chat.ChatRequest;
import com.ivan.researchagent.springai.llm.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * 评估-优化工作流
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/12/周二
 **/
@Slf4j
@Configuration
public class EvaluatorOptimizerGraphConfiguration {

    private int iterationNum = 3;

    @Bean
    public StateGraph contentOptimization(ChatService chatService) throws GraphStateException {
        ChatRequest chatRequest = ChatRequest.builder().build();
        ChatClient chatClient= chatService.getchatClient(chatRequest);

        // 定义全局状态
        OverAllStateFactory stateFactory= () -> {
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy("task", new ReplaceStrategy());
            state.registerKeyAndStrategy("current_content", new ReplaceStrategy());
            state.registerKeyAndStrategy("evaluation_result", new ReplaceStrategy());
            state.registerKeyAndStrategy("optimization_feedback", new ReplaceStrategy());
            state.registerKeyAndStrategy("iteration_count", new ReplaceStrategy());
            state.registerKeyAndStrategy("final_content", new ReplaceStrategy());
            return state;
        };

        // 内容生成节点 - 使用LlmNode
        LlmNode generatorNode= LlmNode.builder()
                .chatClient(chatClient)
                .systemPromptTemplate("你是一位创意内容专家，能够根据任务要求创建高质量的内容，并能够基于反馈进行优化改进。")
                .userPromptTemplate("""
                    {#if optimization_feedback}
                    请基于以下反馈优化内容：

                    原始任务：{task}
                    当前内容：{current_content}
                    优化反馈：{optimization_feedback}

                    请根据反馈意见对内容进行改进。
                    {#else}
                    请为以下任务创建高质量的内容：

                    任务描述：{task}

                    请确保内容：
                    1. 符合任务要求
                    2. 结构清晰
                    3. 表达准确
                    4. 具有吸引力
                    {/if}
                    """)
                .paramsKey("generator_params")
                .outputKey("current_content")
                .build();

        // 评估节点 - 使用LlmNode
        LlmNode evaluatorNode= LlmNode.builder()
                .chatClient(chatClient)
                .systemPromptTemplate("你是一位严格的内容评估专家，能够客观评估内容质量并提供具体的改进建议。")
                .userPromptTemplate("""
                    请评估以下内容是否满足任务要求：

                    任务要求：{task}
                    待评估内容：{current_content}

                    请从以下维度进行评估：
                    1. 内容完整性 (1-10分)
                    2. 准确性 (1-10分)
                    3. 清晰度 (1-10分)
                    4. 吸引力 (1-10分)

                    评估格式：
                    - 如果总体评分>=8分，请在回复开头写"APPROVED"
                    - 如果评分<8分，请提供具体的改进建议

                    评估结果：
                    """)
                .paramsKey("evaluator_params")
                .outputKey("evaluation_result")
                .build();

        // 迭代控制边
        EdgeAction routingEdge = new EdgeAction() {
            @Override
            public String apply(OverAllState state) throws Exception {
                String evaluation= (String) state.value("evaluation_result", StateGraph.END);
                Integer iterationCount= (Integer) state.value("iteration_count", 1);

                // 如果已批准或达到最大迭代次数，结束流程
                if (evaluation.contains("APPROVED") || (iterationCount != null && iterationCount >= iterationNum)) {
                    return"END";
                } else {
                    state.updateState(Map.of("iteration_count", iterationCount + 1));
                    return"CONTINUE";
                }
            }
        };

        // 构建评估-优化工作流
        StateGraph stateGraph = new StateGraph("内容优化工作流", stateFactory)
                .addNode("generator", AsyncNodeAction.node_async(generatorNode))
                .addNode("evaluator", AsyncNodeAction.node_async(evaluatorNode))

                .addEdge(START, "generator")
                .addEdge("generator", "evaluator")

                .addConditionalEdges("evaluator",
                        AsyncEdgeAction.edge_async(routingEdge),
                        Map.of("CONTINUE", "generator", "END", END));

        // 添加 PlantUML 打印
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML,
                "expander flow");
        log.info("\n=== expander UML Flow ===");
        log.info(representation.content());
        log.info("==================================\n");

        return stateGraph;
    }
}
