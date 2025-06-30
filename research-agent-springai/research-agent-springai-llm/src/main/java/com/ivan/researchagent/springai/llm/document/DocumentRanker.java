package com.ivan.researchagent.springai.llm.document;

import com.alibaba.cloud.ai.document.DocumentWithScore;
import com.alibaba.cloud.ai.model.RerankModel;
import com.alibaba.cloud.ai.model.RerankRequest;
import com.alibaba.cloud.ai.model.RerankResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/30/周一
 **/
@Slf4j
public class DocumentRanker implements DocumentPostProcessor {

    private final RerankModel rerankModel;

    private final Double minScore;

    public DocumentRanker(RerankModel rerankModel, Double minScore) {
        this.rerankModel = rerankModel;
        this.minScore = minScore;
    }

    @Override
    public List<Document> process(Query query, List<Document> documents) {
        // 如果文档数量小于等于3，则不进行排序，直接返回原始文档列表
        if (CollectionUtils.isEmpty(documents) || documents.size() <= 3) {
            return documents;
        }

        return doRerank(query, documents);
    }

    /**
     * rerank文档重排序
     * @param query
     * @param documents
     * @return
     */
    private List<Document> doRerank(Query query, List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return documents;
        }

        var rerankRequest = new RerankRequest(query.text(), documents);

        RerankResponse response = rerankModel.call(rerankRequest);
        log.debug("reranked documents: {}", response);
        if (response == null || response.getResults() == null) {
            return documents;
        }

        return response.getResults()
                .stream()
                .filter(doc -> doc != null && doc.getScore() >= minScore)
                .sorted(Comparator.comparingDouble(DocumentWithScore::getScore).reversed())
                .map(DocumentWithScore::getOutput)
                .collect(toList());
    }

}
