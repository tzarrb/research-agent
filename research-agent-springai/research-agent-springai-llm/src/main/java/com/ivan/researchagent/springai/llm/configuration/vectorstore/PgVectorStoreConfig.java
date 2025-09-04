package com.ivan.researchagent.springai.llm.configuration.vectorstore;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

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
@ConditionalOnProperty(name = "spring.ai.vectorstore.pgvector.enable", havingValue = "true", matchIfMissing = true)
public class PgVectorStoreConfig {

    @Bean(name = "pgVectorStore")
    public PgVectorStore vectorStore(@Qualifier("postgresqlJdbcTemplate") JdbcTemplate jdbcTemplate,
                                     DashScopeEmbeddingModel embeddingModel,
                                     PgVectorStoreProperties properties) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(properties.getDimensions())              // Optional: defaults to model dimensions or 1536
                .indexType(properties.getIndexType())                // Optional: defaults to HNSW
                .distanceType(properties.getDistanceType())          // Optional: defaults to COSINE_DISTANCE
                .initializeSchema(true)                             // Optional: defaults to false
                .schemaName("public")                               // Optional: defaults to "public"
                .vectorTableName(properties.getTableName())         // Optional: defaults to "vector_store"
                .maxDocumentBatchSize(10000)                            // Optional: defaults to 10000
                .build();
    }

}
