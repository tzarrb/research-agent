package com.ivan.researchagent.springai.agent.graph.manus;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.constant.SaverConstant;
import com.alibaba.cloud.ai.graph.checkpoint.savers.FileSystemSaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.node.HumanNode;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.ivan.researchagent.springai.agent.graph.core.GraphSerializer;
import com.ivan.researchagent.springai.agent.graph.manus.core.ManusPrompt;
import com.ivan.researchagent.springai.agent.graph.manus.tool.PlanningTool;
import com.ivan.researchagent.springai.agent.graph.manus.tool.ToolBuilder;
import com.ivan.researchagent.springai.llm.model.chat.ChatParams;
import com.ivan.researchagent.springai.llm.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
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
 * @since: 2025/8/22/周五
 **/
@Slf4j
@Configuration
public class ManusAutoConfiguration {

    ChatClient planningClient;

    ChatClient stepClient;

    public ManusAutoConfiguration(ChatService chatService) {

        ChatParams planningChatParams = ChatParams.builder()
                .enableMemory( false)
                .enableStream(false)
                .defaultSystem(ManusPrompt.PLANNING_SYSTEM_PROMPT)
                .toolCallBacks(ToolBuilder.getPlanningToolCalls())
                .build();
        this.planningClient = chatService.getChatClient(planningChatParams);

        ChatParams stepChatParams = ChatParams.builder()
                .enableMemory( false)
                .enableStream(false)
                .defaultSystem(ManusPrompt.STEP_SYSTEM_PROMPT)
                .toolCallBacks(ToolBuilder.getManusAgentToolCalls())
                .build();
        this.stepClient= chatService.getChatClient(stepChatParams);

    }

    @Bean
    public CompiledGraph manusGraph(ChatService chatService) throws GraphStateException {

        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> keyStrategyHashMap = new HashMap<>();

            keyStrategyHashMap.put("plan", new ReplaceStrategy());
            keyStrategyHashMap.put("step_prompt", new ReplaceStrategy());
            keyStrategyHashMap.put("step_output", new ReplaceStrategy());
            keyStrategyHashMap.put("final_output", new ReplaceStrategy());

            return keyStrategyHashMap;
        };

        SupervisorAgent supervisorAgent = new SupervisorAgent(PlanningTool.INSTANCE);

        Path outputPath = null;
        String outputDir = "data/graph/memory/";
        // 确保输出目录存在
        try {
            outputPath = Paths.get(outputDir);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }
        } catch (IOException e) {
            log.warn("无法创建输出目录: {}, 将使用当前目录", outputDir);
        }

        FileSystemSaver fileSystemSaver = new FileSystemSaver(outputPath, new GraphSerializer());
        SaverConfig saverConfig = SaverConfig.builder().register(SaverConstant.FILE, fileSystemSaver).build();
        CompileConfig compileConfig = CompileConfig.builder().saverConfig(saverConfig).build();

        ReactAgent planningAgent = new ReactAgent("planningAgent", this.planningClient, ToolBuilder.getPlanningToolCalls(), 10);
        planningAgent.getAndCompileGraph(compileConfig);

        ReactAgent stepAgent = new ReactAgent("stepAgent", this.stepClient, ToolBuilder.getManusAgentToolCalls(), 10);
        stepAgent.getAndCompileGraph(compileConfig);

        StateGraph graph = new StateGraph("Manus Agent Graph", keyStrategyFactory)
                .addNode("planning_agent", planningAgent.asAsyncNodeAction("input", "plan"))
                .addNode("supervisor_agent", node_async(supervisorAgent))
                .addNode("step_executing_agent", stepAgent.asAsyncNodeAction("step_prompt", "step_output"))

                .addEdge(START, "planning_agent")
                .addEdge("planning_agent", "supervisor_agent")
                .addConditionalEdges("supervisor_agent", edge_async(supervisorAgent::think),
                        Map.of("continue", "step_executing_agent", "end", END))
                .addEdge("step_executing_agent", "supervisor_agent");

        compileConfig = CompileConfig.builder()
                .saverConfig(saverConfig)
                //.interruptBefore(HumanFeedbackNode.class.getSimpleName())
                .build();
        CompiledGraph compiledGraph = graph.compile(compileConfig);

        GraphRepresentation graphRepresentation = compiledGraph.getGraph(GraphRepresentation.Type.PLANTUML);
        System.out.println("\n\n");
        System.out.println(graphRepresentation.content());
        System.out.println("\n\n");

        return compiledGraph;
    }


    @Bean
    public CompiledGraph manusHumanGraph(ChatService chatService) throws GraphStateException {

        OverAllStateFactory stateFactory = () -> {
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy("plan", new ReplaceStrategy());
            state.registerKeyAndStrategy("step_prompt", new ReplaceStrategy());
            state.registerKeyAndStrategy("step_output", new ReplaceStrategy());
            state.registerKeyAndStrategy("final_output", new ReplaceStrategy());

            return state;
        };

        // 人工反馈节点
        HumanNode humanNode = new HumanNode();

        // 监督协调者节点
        SupervisorAgent supervisorAgent = new SupervisorAgent(PlanningTool.INSTANCE);


        // 计划制定节点
        ReactAgent planningAgent = new ReactAgent("planningAgent", this.planningClient, ToolBuilder.getPlanningToolCalls(),
                10);
        planningAgent.getAndCompileGraph();

        // 步骤执行节点
        ReactAgent stepAgent = new ReactAgent("stepAgent", this.stepClient, ToolBuilder.getManusAgentToolCalls(), 10);
        stepAgent.getAndCompileGraph();

        StateGraph graph = new StateGraph(stateFactory)
                .addNode("planning_agent", planningAgent.asAsyncNodeAction("input", "plan"))
                .addNode("human", node_async(humanNode))
                .addNode("supervisor_agent", node_async(supervisorAgent))
                .addNode("step_executing_agent", stepAgent.asAsyncNodeAction("step_prompt", "step_output"))

                .addEdge(START, "planning_agent")
                .addEdge("planning_agent", "human")
                .addConditionalEdges("human", edge_async(humanNode::think),
                        Map.of("planning_agent", "planning_agent", "supervisor_agent", "supervisor_agent"))
                .addConditionalEdges("supervisor_agent", edge_async(supervisorAgent::think),
                        Map.of("continue", "step_executing_agent", "end", END))
                .addEdge("step_executing_agent", "supervisor_agent");

        CompiledGraph compiledGraph = graph.compile();

        GraphRepresentation graphRepresentation = compiledGraph.getGraph(GraphRepresentation.Type.PLANTUML);
        System.out.println("\n\n");
        System.out.println(graphRepresentation.content());
        System.out.println("\n\n");

        return compiledGraph;
    }
}
