package com.ivan.researchagent.springai.agent.graph.agent.translate.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import reactor.core.publisher.Flux;

import java.util.HashMap;
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
public class TranslateNode implements NodeAction {

    static String PROMPT_TEMPLATE = """
            Given a user query, translate it to {targetLanguage}.
            If the query is already in {targetLanguage}, return it unchanged.
            If you don't know the language of the query, return it unchanged.
            Do not add explanations nor any other text.
            
            Original query: {query}
            
            Translated query:
            """;
    private static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = new PromptTemplate(PROMPT_TEMPLATE);

    private final String  TARGET_LANGUAGE= "English";

    private final ChatClient chatClient;

    public TranslateNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String query = state.value("query", "");
        String targetLanguage = state.value("translate_language", TARGET_LANGUAGE);

        Flux<String> streamResult = this.chatClient
                .prompt()
                .user((user) -> user
                        .text(DEFAULT_PROMPT_TEMPLATE.getTemplate())
                        .param("targetLanguage", targetLanguage)
                        .param("query", query)
                )
                .stream()
                .content();
        String result = streamResult.reduce("", (acc, item) -> acc + item).block();

        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("translate_content", result);
        return resultMap;
    }
}
