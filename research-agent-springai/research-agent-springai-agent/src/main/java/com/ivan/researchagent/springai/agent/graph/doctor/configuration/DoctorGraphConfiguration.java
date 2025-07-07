package com.ivan.researchagent.springai.agent.graph.doctor.configuration;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.ivan.researchagent.springai.agent.graph.doctor.dispatcher.HumanFeedbackDispatcher;
import com.ivan.researchagent.springai.agent.graph.doctor.node.DoctorOperateNode;
import com.ivan.researchagent.springai.agent.graph.doctor.node.DoctorQueryNode;
import com.ivan.researchagent.springai.agent.graph.doctor.node.DoctorUpdateNode;
import com.ivan.researchagent.springai.agent.graph.doctor.node.HumanFeedbackNode;
import com.ivan.researchagent.springai.agent.tool.DoctorTools;
import com.ivan.researchagent.springai.llm.service.ChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/7/3/周四
 **/
@Slf4j
@Configuration
public class DoctorGraphConfiguration {

    @Resource
    private ChatService chatService;

    @Resource
    private DoctorTools doctorTools;

    @Bean
    public StateGraph doctorGraph() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> keyStrategyHashMap = new HashMap<>();
            // 用户输入
            keyStrategyHashMap.put("query", new ReplaceStrategy());
            keyStrategyHashMap.put("thread_id", new ReplaceStrategy());

            // 搜索结果
            keyStrategyHashMap.put("max_result", new ReplaceStrategy());
            keyStrategyHashMap.put("query_result", new ReplaceStrategy());

            // 人类反馈
            keyStrategyHashMap.put("feed_back", new ReplaceStrategy());
            keyStrategyHashMap.put("human_next_node", new ReplaceStrategy());

            // 是否需要处理
            keyStrategyHashMap.put("provider_id", new ReplaceStrategy());
            keyStrategyHashMap.put("operate_type", new ReplaceStrategy());
            return keyStrategyHashMap;
        };

        String queryNode = DoctorQueryNode.class.getSimpleName();
        String operateNode = DoctorOperateNode.class.getSimpleName();
        String updateNode = DoctorUpdateNode.class.getSimpleName();
        String feedbackNode = HumanFeedbackNode.class.getSimpleName();
        StateGraph stateGraph = new StateGraph(keyStrategyFactory)
                .addNode(queryNode, node_async(new DoctorQueryNode(chatService, doctorTools)))
                .addNode(operateNode, node_async(new DoctorOperateNode(chatService, doctorTools)))
                .addNode(updateNode, node_async(new DoctorUpdateNode(chatService, doctorTools)))
                .addNode(feedbackNode, node_async(new HumanFeedbackNode(chatService)))

                .addEdge(StateGraph.START, queryNode)
                .addEdge(queryNode, feedbackNode)
                .addEdge(operateNode, feedbackNode)
                .addEdge(updateNode, feedbackNode)
                .addConditionalEdges(feedbackNode, AsyncEdgeAction.edge_async((new HumanFeedbackDispatcher())),
                        Map.of(queryNode, queryNode, operateNode, operateNode, updateNode, updateNode, StateGraph.END, StateGraph.END));

        // 添加 PlantUML 打印
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML, "doctor graph flow");
        log.info("\n=============== expander UML Flow ===============");
        log.info(representation.content());
        log.info("===================================================\n");

        return stateGraph;
    }


}
