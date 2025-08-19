package com.ivan.researchagent.springai.llm.configuration.vectorstore;

import org.elasticsearch.client.RestClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import org.springframework.ai.vectorstore.elasticsearch.autoconfigure.ElasticsearchVectorStoreProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/12/周二
 **/
@Configuration
@ConditionalOnProperty(name = "spring.ai.vectorstore.elasticsearch.enable", havingValue = "true", matchIfMissing = false)
public class ElasticsearchVectorStoreConfig {

    @Bean(name = "elasticsearchVectorStore")
    public ElasticsearchVectorStore elasticsearchVectorStore(RestClient restClient,
                                                             EmbeddingModel embeddingModel,
                                                             ElasticsearchVectorStoreProperties properties) {
        ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();
        options.setIndexName(properties.getIndexName());    // Optional: defaults to "spring-ai-document-index"
        options.setSimilarity(properties.getSimilarity());           // Optional: defaults to COSINE
        options.setDimensions(properties.getDimensions());             // Optional: defaults to model dimensions or 1536

        return ElasticsearchVectorStore.builder(restClient, embeddingModel)
                .options(options)                     // Optional: use custom options
                .initializeSchema(true)               // Optional: defaults to false
                .batchingStrategy(new TokenCountBatchingStrategy()) // Optional: defaults to TokenCountBatchingStrategy
                .build();
    }

}
