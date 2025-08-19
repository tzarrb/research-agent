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
  * 代码生成节点 - 负责生成和改进代码
  */

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.node.LlmNode;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;

import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

public class CodeGeneratorNode implements NodeAction {
    private final LlmNode llmNode;

    // 代码生成提示词
    private static final String CODE_PROMPT="""
        你是一位资深的软件工程师，专注于编写高质量、可维护的代码。
        请根据需求生成清晰、规范的代码。
        如果收到代码审查意见，请基于建议改进代码。
        关注代码的可读性、性能、安全性和错误处理。
        只返回代码和必要的注释。
    """;

    public CodeGeneratorNode(ChatClient chatClient) {
        this.llmNode = LlmNode.builder()
            .systemPromptTemplate(new SystemPromptTemplate(CODE_PROMPT).render())
            .chatClient(chatClient)
            .messagesKey("messages")
            .build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state)throws Exception {
        List<Message> messages = (List<Message>) state.value("messages").get();

        StateGraph subGraph= new StateGraph(() -> Map.of("messages", new AppendStrategy()))
                .addNode("generate", node_async(llmNode))
                .addEdge(START, "generate")
                .addEdge("generate", END);

        return Map.of("messages", subGraph.compile().invoke(Map.of("messages", messages))
                        .get().value("messages").orElseThrow());
    }
}
