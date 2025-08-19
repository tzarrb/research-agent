package com.ivan.researchagent.springai.agent.graph.agent.travel;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.google.common.collect.Lists;
import com.ivan.researchagent.springai.llm.model.chat.ChatRequest;
import com.ivan.researchagent.springai.llm.service.ChatService;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * ReAct推理执行
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/14/周四
 **/
@Configuration
public class TravelAutoconfiguration {

    @Bean
    public ReactAgent travelAgent(ChatService chatService, ToolCallbackResolver resolver) throws GraphStateException {
//        ChatClient chatClient = ChatClient.builder(chatModel)
//                .defaultToolNames("getWeatherFunction")
//                .defaultAdvisors(new SimpleLoggerAdvisor())
//                .defaultOptions(OpenAiChatOptions.builder().internalToolExecutionEnabled(false).build())
//                .build();

        ChatRequest chatRequest = ChatRequest.builder()
                .enableMemory(true)
                .enableStream(false)
                .toolNames(Lists.newArrayList("getWeatherFunction"))
                .build();
        ChatClient chatClient= chatService.getchatClient(chatRequest);

        return ReactAgent.builder()
                .name("Travel Plan Agent")
                .chatClient(chatClient)
                .resolver(resolver)
                .maxIterations(10)
                .build();
    }

    @Bean
    public CompiledGraph travelGraph(@Qualifier("travelAgent") ReactAgent reactAgent) throws GraphStateException {

        GraphRepresentation graphRepresentation = reactAgent.getStateGraph()
                .getGraph(GraphRepresentation.Type.PLANTUML);

        System.out.println("\n\n");
        System.out.println(graphRepresentation.content());
        System.out.println("\n\n");

        return reactAgent.getAndCompileGraph();
    }

    @Bean
    public RestClient.Builder createRestClient() {

        // 2. 创建 RequestConfig 并设置超时
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.of(10, TimeUnit.MINUTES)) // 设置连接超时
                .setResponseTimeout(Timeout.of(10, TimeUnit.MINUTES))
                .setConnectionRequestTimeout(Timeout.of(10, TimeUnit.MINUTES))
                .build();

        // 3. 创建 CloseableHttpClient 并应用配置
        HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();

        // 4. 使用 HttpComponentsClientHttpRequestFactory 包装 HttpClient
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        // 5. 创建 RestClient 并设置请求工厂
        return RestClient.builder().requestFactory(requestFactory);
    }

}
