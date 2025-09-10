package com.ivan.researchagent.springai.llm.tools.search.tavily;

import com.alibaba.fastjson.JSON;
import com.ivan.researchagent.springai.llm.model.tool.TavilySearchRequest;
import com.ivan.researchagent.springai.llm.model.tool.WebSearchRequest;
import com.ivan.researchagent.springai.llm.model.tool.WebSearchResponse;
import com.ivan.researchagent.springai.llm.tools.search.WebSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.alibaba.cloud.ai.toolcalling.common.CommonToolCallConstants.DEFAULT_USER_AGENTS;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/25/周三
 **/
@Slf4j
public class TavilySearchService extends WebSearchService {

    private final String URL = "https://api.tavily.com/search";

    private final TavilySearchProperties properties;

    public TavilySearchService(TavilySearchProperties properties) {
        Consumer<HttpHeaders> consumer = headers -> {
            headers.add(HttpHeaders.USER_AGENT,
                    DEFAULT_USER_AGENTS[ThreadLocalRandom.current().nextInt(DEFAULT_USER_AGENTS.length)]);
            headers.add(HttpHeaders.REFERER, "https://api.tavily.com/");
            headers.add(HttpHeaders.CONNECTION, "keep-alive");
            headers.add(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9");
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey());
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        };
        this.webClient = WebClient.builder()
                .baseUrl(Optional.ofNullable(properties.getBaseUrl()).orElse(URL))
                .defaultHeaders(consumer)
                .build();
        this.properties = properties;
    }

    @Override
    public WebSearchResponse search(WebSearchRequest request) {
        TavilySearchRequest build = TavilySearchRequest.builder()
                .query(request.query())
                .topic(properties.getTopic())
                .searchDepth(properties.getSearchDepth())
                .chunksPerSource(properties.getChunksPerSource())
                .maxResults(properties.getMaxResults())
                .days(properties.getDays())
                .includeRawContent(properties.isIncludeRawContent())
                .includeImages(properties.isIncludeImages())
                .includeImageDescriptions(properties.isIncludeImageDescriptions())
                .includeAnswer(properties.isIncludeAnswer())
                .build();
        WebSearchResponse response = webClient.post()
                .bodyValue(build)
                .retrieve()
                .bodyToMono(WebSearchResponse.class)
                .block();
        log.debug("TavilySearchApi search response: {}", JSON.toJSONString(response));
        return response;
    }

    @Override
    public WebSearchResponse apply(WebSearchRequest request) {
        return search(request);
    }
}
