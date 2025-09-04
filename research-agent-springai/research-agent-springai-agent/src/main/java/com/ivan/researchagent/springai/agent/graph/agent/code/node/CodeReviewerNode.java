package com.ivan.researchagent.springai.agent.graph.agent.code.node;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/14/周四
 **/
/**
  * 代码审查节点 - 负责评估代码质量并提供改进建议
  */

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.node.LlmNode;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;

import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

public class CodeReviewerNode implements NodeAction {
    private final LlmNode llmNode;

    // 代码审查提示词
    private static final String REVIEW_PROMPT="""
        你是一位资深的代码审查专家，负责评估代码质量。
        请从以下方面进行评估：
        - 代码可读性和命名规范
        - 性能优化和算法效率
        - 安全性和错误处理
        - 设计模式和最佳实践

        请提供具体的改进建议，用中文回答。
    """;

    public CodeReviewerNode(ChatClient chatClient) {
        this.llmNode = LlmNode.builder()
            .systemPromptTemplate(new SystemPromptTemplate(REVIEW_PROMPT).render())
            .chatClient(chatClient)
            .messagesKey("messages")
            .build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state)throws Exception {
        List<Message> messages = (List<Message>) state.value("messages").get();

        StateGraph subGraph= new StateGraph(() -> Map.of("messages", new AppendStrategy()))
                .addNode("review", node_async(llmNode))
                .addEdge(START, "review")
                .addEdge("review", END);

        List<Message> result = (List<Message>) subGraph.compile()
            .invoke(Map.of("messages", messages)).get().value("messages").orElseThrow();

        int size= result.size();
        if (size > 0) result.set(size - 1, new UserMessage(result.get(size - 1).getText()));

        return Map.of("messages", result);
    }
}
