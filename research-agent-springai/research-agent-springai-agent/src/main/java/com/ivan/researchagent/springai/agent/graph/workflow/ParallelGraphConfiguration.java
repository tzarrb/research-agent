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
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * 并行
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/12/周二
 **/
@Slf4j
@Configuration
public class ParallelGraphConfiguration {

    @Bean
    public StateGraph marketAnalysisParallel(ChatService chatService) throws GraphStateException {
        ChatRequest chatRequest = ChatRequest.builder().build();
        ChatClient chatClient= chatService.getchatClient(chatRequest);

        // 定义全局状态
        OverAllStateFactory stateFactory= () -> {
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy("market_change", new ReplaceStrategy());
            state.registerKeyAndStrategy("customer_impact", new ReplaceStrategy());
            state.registerKeyAndStrategy("employee_impact", new ReplaceStrategy());
            state.registerKeyAndStrategy("investor_impact", new ReplaceStrategy());
            state.registerKeyAndStrategy("supplier_impact", new ReplaceStrategy());
            state.registerKeyAndStrategy("final_report", new ReplaceStrategy());
            return state;
        };

        // 并行分析节点 - 使用LlmNode
        LlmNode customerAnalysisNode= LlmNode.builder()
                .chatClient(chatClient)
                .systemPromptTemplate("你是一位资深的客户关系专家，深度理解客户行为和市场动态对客户群体的影响。")
                .userPromptTemplate("市场变化情况：{market_change}\n\n请从客户角度分析：\n1. 对客户需求的影响\n2. 对客户行为的影响\n3. 对客户满意度的影响\n4. 应对建议")
                .paramsKey("customer_params")
                .outputKey("customer_impact")
                .build();

        LlmNode employeeAnalysisNode= LlmNode.builder()
                .chatClient(chatClient)
                .systemPromptTemplate("你是一位人力资源专家，擅长分析市场变化对员工和组织的影响。")
                .userPromptTemplate("市场变化情况：{market_change}\n\n请从员工角度分析：\n1. 对工作岗位的影响\n2. 对技能要求的影响\n3. 对职业发展的影响\n4. 人力资源策略建议")
                .paramsKey("employee_params")
                .outputKey("employee_impact")
                .build();

        LlmNode investorAnalysisNode= LlmNode.builder()
                .chatClient(chatClient)
                .systemPromptTemplate("你是一位投资分析专家，能够深入分析市场变化对投资价值和风险的影响。")
                .userPromptTemplate("市场变化情况：{market_change}\n\n请从投资者角度分析：\n1. 对投资回报的影响\n2. 对风险评估的影响\n3. 对投资策略的影响\n4. 投资建议")
                .paramsKey("investor_params")
                .outputKey("investor_impact")
                .build();

        LlmNode supplierAnalysisNode= LlmNode.builder()
                .chatClient(chatClient)
                .systemPromptTemplate("你是一位供应链管理专家，深度了解市场变化对供应商生态的影响。")
                .userPromptTemplate("市场变化情况：{market_change}\n\n请从供应商角度分析：\n1. 对供应链的影响\n2. 对成本结构的影响\n3. 对合作关系的影响\n4. 供应链优化建议")
                .paramsKey("supplier_params")
                .outputKey("supplier_impact")
                .build();

        // 结果汇总节点 - 使用LlmNode
        LlmNode summaryNode= LlmNode.builder()
                .chatClient(chatClient)
                .systemPromptTemplate("你是一位资深的商业分析师，擅长整合多维度分析结果，生成具有洞察力的综合报告。")
                .userPromptTemplate("""
                    基于以下各利益相关方的分析结果，请生成一份综合的市场影响报告：

                    客户影响分析：
                    {customer_impact}

                    员工影响分析：
                    {employee_impact}

                    投资者影响分析：
                    {investor_impact}

                    供应商影响分析：
                    {supplier_impact}

                    请生成包含以下内容的综合报告：
                    1. 执行摘要
                    2. 各方影响总结
                    3. 关键风险和机遇
                    4. 战略建议
                    5. 行动计划
                    """)
                .paramsKey("summary_params")
                .outputKey("final_report")
                .build();

        // 构建并行工作流 - 使用node_async实现真正的并行执行
        StateGraph stateGraph = new StateGraph("市场分析并行工作流", stateFactory)
                .addNode("customer_analysis", node_async(customerAnalysisNode))
                .addNode("employee_analysis", node_async(employeeAnalysisNode))
                .addNode("investor_analysis", node_async(investorAnalysisNode))
                .addNode("supplier_analysis", node_async(supplierAnalysisNode))
                .addNode("summary",

                         node_async(summaryNode))  // 汇总节点不需要异步，等待所有并行节点完成
                // 所有分析节点从START开始（并行执行）
                .addEdge(START, "customer_analysis")
                .addEdge(START, "employee_analysis")
                .addEdge(START, "investor_analysis")
                .addEdge(START, "supplier_analysis")
                // 所有分析完成后进入汇总
                .addEdge("customer_analysis", "summary")
                .addEdge("employee_analysis", "summary")
                .addEdge("investor_analysis", "summary")
                .addEdge("supplier_analysis", "summary")
                .addEdge("summary", END);

        // 添加 PlantUML 打印
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML,
                "expander flow");
        log.info("\n=== expander UML Flow ===");
        log.info(representation.content());
        log.info("==================================\n");

        return stateGraph;
    }
}
