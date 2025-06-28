package com.ivan.researchagent.springai.llm.tools.search.baidusearch;

import com.alibaba.cloud.ai.toolcalling.common.CommonToolCallUtils;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.ivan.researchagent.springai.llm.model.tool.WebSearchResponse;
import com.ivan.researchagent.springai.llm.tools.search.WebSearchApi;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

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
public class BaiduSearchApi extends WebSearchApi {

    private final String URL = "https://www.baidu.com/";

    private final BaiduSearchProperties properties;

    public BaiduSearchApi(BaiduSearchProperties properties) {
        Consumer<HttpHeaders> consumer = headers -> {
            headers.add(HttpHeaders.USER_AGENT,
                    DEFAULT_USER_AGENTS[ThreadLocalRandom.current().nextInt(DEFAULT_USER_AGENTS.length)]);
            headers.add(HttpHeaders.REFERER, "https://www.baidu.com/");
            headers.add(HttpHeaders.CONNECTION, "keep-alive");
            headers.add(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9");
        };
        this.webClient = WebClient.builder()
                .baseUrl(Optional.ofNullable(properties.getBaseUrl()).orElse(URL))
                .defaultHeaders(consumer)
                .build();
        this.properties = properties;
    }

    @Override
    public WebSearchResponse search(String query) {
        int limit = properties.getMaxResults() != null ? properties.getMaxResults() : 5;
        String url = Optional.ofNullable(properties.getBaseUrl()).orElse(URL) + query;

        String html = webClient
                .get()
                .uri(url)
                .acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<WebSearchResponse.ResultInfo> results = CommonToolCallUtils.handleResponse(html, this::parseHtml, log);

        if (CollectionUtils.isEmpty(results)) {
            return null;
        }

        log.info("baidu search: {},result number:{}", query, results.size());
        for (WebSearchResponse.ResultInfo d : results) {
            log.info("{}\n{}\n{}", d.getTitle(), d.getContent(), d.getUrl());
        }

        WebSearchResponse response = new WebSearchResponse();
        response.setResults(results.subList(0, Math.min(results.size(), limit)));
        return response;
    }

    private List<WebSearchResponse.ResultInfo> parseHtml(String htmlContent) {
        try {
            Document doc = Jsoup.parse(htmlContent);
            Element contentLeft = doc.selectFirst("div#content_left");
            Elements divContents = contentLeft.children();
            List<WebSearchResponse.ResultInfo> listData = new ArrayList<>();

            for (Element div : divContents) {
                if (!div.hasClass("c-container")) {
                    continue;
                }
                String title = "";
                String abstractText = "";
                String sourceUrl = div.attr("mu");

                try {
                    if (div.hasClass("xpath-log") || div.hasClass("result-op")) {
                        if (div.selectFirst("h3") != null) {
                            title = div.selectFirst("h3").text().trim();
                        }
                        else {
                            title = div.text().trim().split("\n", 2)[0];
                        }

                        if (div.selectFirst("div.c-abstract") != null) {
                            abstractText = div.selectFirst("div.c-abstract").text().trim();
                        }
                        else if (div.selectFirst("div") != null) {
                            abstractText = div.selectFirst("div").text().trim();
                        }
                        else {
                            abstractText = div.text().trim().split("\n", 2)[1].trim();
                        }
                    }
                    else if ("se_com_default".equals(div.attr("tpl"))) {
                        if (div.selectFirst("h3") != null) {
                            title = div.selectFirst("h3").text().trim();
                        }
                        else {
                            title = div.children().get(0).text().trim();
                        }

                        if (div.selectFirst("div.c-abstract") != null) {
                            abstractText = div.selectFirst("div.c-abstract").text().trim();
                        }
                        else if (div.selectFirst("div") != null) {
                            abstractText = div.selectFirst("div").text().trim();
                        }
                        else {
                            abstractText = div.text().trim();
                        }
                    }
                    else {
                        continue;
                    }
                }
                catch (Exception e) {
                    log.error("Failed to parse search result: {}", e.getMessage());
                    continue;
                }

                listData.add(new WebSearchResponse.ResultInfo(title, abstractText, sourceUrl, 0, null));
            }

            return listData;
        }
        catch (Exception e) {
            log.error("Failed to parse HTML content: {}", e.getMessage());
            return null;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("Baidu search API request")
    public record Request(
            @JsonProperty(required = true, value = "query") @JsonPropertyDescription("The search query") String query,
            @JsonProperty(required = false, value = "limit") @JsonPropertyDescription("Maximum number of results to return") Integer limit) {

    }

    /**
     * Baidu search Function response.
     */
    @JsonClassDescription("Baidu search API response")
    public record Response(List<SearchResult> results) {

    }

    public record SearchResult(String title, String abstractText, String sourceUrl) {

    }

}
