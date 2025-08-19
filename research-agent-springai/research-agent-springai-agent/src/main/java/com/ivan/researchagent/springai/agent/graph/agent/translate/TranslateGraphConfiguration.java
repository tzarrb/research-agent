package com.ivan.researchagent.springai.agent.graph.agent.translate;

import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.ivan.researchagent.springai.agent.graph.agent.translate.node.ExpanderNode;
import com.ivan.researchagent.springai.agent.graph.agent.translate.node.MergeResultsNode;
import com.ivan.researchagent.springai.agent.graph.agent.translate.node.TranslateNode;
import com.ivan.researchagent.springai.llm.model.chat.ChatRequest;
import com.ivan.researchagent.springai.llm.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * 并行工作流-扩展翻译
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/12/周二
 **/
@Slf4j
@Configuration
public class TranslateGraphConfiguration {

    @Bean
    public StateGraph expanderTranslateGraph(ChatService chatService) throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> keyStrategyHashMap = new HashMap<>();

            // 用户输入
            keyStrategyHashMap.put("query", new ReplaceStrategy());

            keyStrategyHashMap.put("expander_number", new ReplaceStrategy());
            keyStrategyHashMap.put("expander_content", new ReplaceStrategy());

            keyStrategyHashMap.put("translate_language", new ReplaceStrategy());
            keyStrategyHashMap.put("translate_content", new ReplaceStrategy());

            keyStrategyHashMap.put("merge_result", new ReplaceStrategy());

            return keyStrategyHashMap;
        };

        ChatRequest chatRequest = ChatRequest.builder().build();
        ChatClient chatClient= chatService.getchatClient(chatRequest);

        // 构建并行工作流
        StateGraph stateGraph = new StateGraph(keyStrategyFactory)
                .addNode("expander", AsyncNodeAction.node_async(new ExpanderNode(chatClient)))
                .addNode("translate", AsyncNodeAction.node_async(new TranslateNode(chatClient)))
                .addNode("merge", AsyncNodeAction.node_async(new MergeResultsNode()))

                .addEdge(StateGraph.START, "expander")
                .addEdge(StateGraph.START, "translate")
                .addEdge("translate", "merge")
                .addEdge("expander", "merge")

                .addEdge("merge", StateGraph.END);

        // 添加 PlantUML 打印
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML, "expander flow");
        log.info("\n=== expander UML Flow ===");
        log.info(representation.content());
        log.info("==================================\n");

        return stateGraph;
    }
}
