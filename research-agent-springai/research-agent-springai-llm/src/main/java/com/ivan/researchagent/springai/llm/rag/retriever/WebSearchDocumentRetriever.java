package com.ivan.researchagent.springai.llm.rag.retriever;

import com.google.common.collect.Lists;
import com.ivan.researchagent.springai.llm.model.tool.WebSearchRequest;
import com.ivan.researchagent.springai.llm.model.tool.WebSearchResponse;
import com.ivan.researchagent.springai.llm.tools.search.WebSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/24/周二
 **/
@Slf4j
public class WebSearchDocumentRetriever implements DocumentRetriever {

    private WebSearchService webSearchService;

    private final int maxResults;

    private WebSearchDocumentRetriever(Builder builder) {
        this.maxResults = builder.maxResults;
        this.webSearchService = builder.webSearchService;
    }

    @NotNull
    @Override
    public List<Document> retrieve(@Nullable Query query) {
        // 搜索
        WebSearchRequest request = new WebSearchRequest(query.text(), maxResults);
        WebSearchResponse webSearchResponse = webSearchService.search(request);
        if (CollectionUtils.isEmpty(webSearchResponse.getResults())) {
            return Lists.newArrayList();
        }

        List<Document> documents = webSearchResponse.getResults().stream().map(resultInfo -> {
            Document document = Document.builder()
                    .text(resultInfo.getContent())
                    .score(resultInfo.getScore())
                    .build();
            return document;
        }).toList();

        return documents;
    }

    public static WebSearchDocumentRetriever.Builder builder() {
        return new WebSearchDocumentRetriever.Builder();
    }


    public static final class Builder {

        private int maxResults;

        private WebSearchService webSearchService;

        public WebSearchDocumentRetriever.Builder maxResults(int maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        public WebSearchDocumentRetriever.Builder webSearchApi(WebSearchService webSearchService) {
            this.webSearchService = webSearchService;
            return this;
        }

        public WebSearchDocumentRetriever build() {
            return new WebSearchDocumentRetriever(this);
        }
    }

}
