package com.ivan.researchagent.springai.agent.graph.workflow;

import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.OverAllStateFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.node.LlmNode;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.ivan.researchagent.springai.llm.model.chat.ChatRequest;
import com.ivan.researchagent.springai.llm.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * 编排者-工作者工作流项目
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/12/周二
 **/
@Slf4j
@Configuration
public class OrchestratorWorkersGraphConfiguration {

    @Bean
    public StateGraph projectOrchestrator(ChatService chatService) throws GraphStateException {
        ChatRequest chatRequest = ChatRequest.builder().build();
        ChatClient chatClient= chatService.getchatClient(chatRequest);

        // 定义全局状态
        OverAllStateFactory stateFactory= () -> {
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy("task_description", new ReplaceStrategy());
            state.registerKeyAndStrategy("task_analysis", new ReplaceStrategy());
            state.registerKeyAndStrategy("subtasks", new ReplaceStrategy());
            state.registerKeyAndStrategy("technical_doc", new ReplaceStrategy());
            state.registerKeyAndStrategy("user_doc", new ReplaceStrategy());
            state.registerKeyAndStrategy("final_documentation", new ReplaceStrategy());
            return state;
        };

        // 编排者节点 - 使用LlmNode分析任务并分解
        LlmNode orchestratorNode= LlmNode.builder()
                .chatClient(chatClient)
                .systemPromptTemplate("你是一位资深的项目管理专家，擅长分析复杂任务并将其分解为可执行的子任务。")
                .userPromptTemplate("""
                    请分析以下任务并制定详细的执行计划：

                    任务描述：{task_description}

                    请提供：
                    1. 任务分析和理解
                    2. 需要生成的文档类型
                    3. 每个子任务的具体要求
                    4. 执行优先级和依赖关系
                    """)
                .paramsKey("orchestrator_params")
                .outputKey("task_analysis")
                .build();

        // 工作者节点 - 技术文档生成
        LlmNode technicalWorkerNode= LlmNode.builder()
                .chatClient(chatClient)
                .systemPromptTemplate("你是一位资深的技术文档工程师，擅长编写清晰、准确、完整的技术文档。")
                .userPromptTemplate("""
                    基于以下信息生成专业的技术文档：

                    原始任务：{task_description}
                    任务分析：{task_analysis}

                    请生成包含以下内容的技术文档：
                    1. API接口规范
                    2. 请求/响应参数详细说明
                    3. 错误码和异常处理
                    4. 技术实现细节
                    5. 性能和安全考虑
                    """)
                .paramsKey("technical_params")
                .outputKey("technical_doc")
                .build();

        // 工作者节点 - 用户文档生成
        LlmNode userWorkerNode= LlmNode.builder()
                .chatClient(chatClient)
                .systemPromptTemplate("你是一位用户体验专家，擅长编写用户友好、易于理解的使用指南。")
                .userPromptTemplate("""
                    基于以下信息生成用户友好的使用文档：

                    原始任务：{task_description}
                    任务分析：{task_analysis}

                    请生成包含以下内容的用户文档：
                    1. 快速开始指南
                    2. 详细使用步骤
                    3. 实际使用示例
                    4. 常见问题解答
                    5. 最佳实践建议
                    """)
                .paramsKey("user_params")
                .outputKey("user_doc")
                .build();

        // 结果整合节点
        LlmNode integrationNode= LlmNode.builder()
                .chatClient(chatClient)
                .systemPromptTemplate("你是一位文档架构师，擅长整合不同类型的文档，创建结构清晰、内容完整的综合文档。")
                .userPromptTemplate("""
                    请整合以下文档，生成完整的API文档：

                    技术文档：
                    {technical_doc}

                    用户文档：
                    {user_doc}

                    请生成包含以下结构的完整文档：
                    1. 概述和介绍
                    2. 快速开始
                    3. 详细API参考
                    4. 使用示例
                    5. 常见问题
                    6. 附录和参考资料
                    """)
                .paramsKey("integration_params")
                .outputKey("final_documentation")
                .build();

        // 构建编排者-工作者工作流
        StateGraph stateGraph = new StateGraph("文档生成编排工作流", stateFactory)
                .addNode("orchestrator", AsyncNodeAction.node_async(orchestratorNode))
                .addNode("technical_worker", AsyncNodeAction.node_async(technicalWorkerNode))
                .addNode("user_worker", AsyncNodeAction.node_async(userWorkerNode))
                .addNode("integrator", AsyncNodeAction.node_async(integrationNode))

                .addEdge(START, "orchestrator")
                // 编排者完成后，工作者并行执行
                .addEdge("orchestrator", "technical_worker")
                .addEdge("orchestrator", "user_worker")
                // 工作者完成后进入整合阶段
                .addEdge("technical_worker", "integrator")
                .addEdge("user_worker", "integrator")
                .addEdge("integrator", END);

        // 添加 PlantUML 打印
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML,
                "expander flow");
        log.info("\n=== expander UML Flow ===");
        log.info(representation.content());
        log.info("==================================\n");

        return stateGraph;
    }
}
