package com.ivan.researchagent.springai.agent.graph.agent.translate.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/12/周二
 **/
public class ExpanderNode implements NodeAction {

    static final String DEFAULT_PROMPT = """
    You are an expert at information retrieval and search optimization.
    Your task is to generate {number} different versions of the given query.
    
    Each variant must cover different perspectives or aspects of the topic,
    while maintaining the core intent of the original query. The goal is to 
    expand the search space and improve the chances of finding relevant information.
    
    Do not explain your choices or add any other text.
    Provide the query variants separated by newlines.
    
    Original query: {query}
    
    Query variants:
    """;
    private static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = new PromptTemplate(DEFAULT_PROMPT);

    private final Integer NUMBER = 3;

    private final ChatClient chatClient;

    public ExpanderNode(ChatClient  chatClient ) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String query = state.value("query", "");
        Integer expanderNumber = state.value("expander_number", this.NUMBER);


        Flux<String> streamResult = this.chatClient
                .prompt()
                .user((user) -> user
                        .text(DEFAULT_PROMPT_TEMPLATE.getTemplate())
                        .param("number", expanderNumber)
                        .param("query", query)
                )
                .stream()
                .content();
        String result = streamResult.reduce("", (acc, item) -> acc + item).block();
        List<String> queryVariants = Arrays.asList(result.split("\n"));

        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("expander_content", queryVariants);
        return resultMap;
    }
}
