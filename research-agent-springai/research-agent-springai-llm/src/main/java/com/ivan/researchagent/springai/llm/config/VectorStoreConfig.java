package com.ivan.researchagent.springai.llm.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import io.github.pigmesh.ai.deepseek.core.EmbeddingClient;
import jakarta.annotation.Resource;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import org.springframework.ai.vectorstore.elasticsearch.autoconfigure.ElasticsearchVectorStoreProperties;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/7/24/周四
 **/
@Configuration
public class VectorStoreConfig {

//    @Bean(name = "pgVectorStore")
//    public PgVectorStore vectorStore(@Qualifier("postgresqlJdbcTemplate") JdbcTemplate jdbcTemplate,
//                                   EmbeddingModel embeddingModel,
//                                   PgVectorStoreProperties properties) {
//        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
//                .dimensions(properties.getDimensions())              // Optional: defaults to model dimensions or 1536
//                .indexType(properties.getIndexType())                // Optional: defaults to HNSW
//                .distanceType(properties.getDistanceType())          // Optional: defaults to COSINE_DISTANCE
//                .initializeSchema(true)                             // Optional: defaults to false
//                .schemaName("public")                               // Optional: defaults to "public"
//                .vectorTableName(properties.getTableName())         // Optional: defaults to "vector_store"
//                .maxDocumentBatchSize(10000)                            // Optional: defaults to 10000
//                .build();
//    }
//
//    @Bean(name = "elasticsearchVectorStore")
//    public ElasticsearchVectorStore elasticsearchVectorStore(RestClient restClient,
//                                                             EmbeddingModel embeddingModel,
//                                                             ElasticsearchVectorStoreProperties properties) {
//        ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();
//        options.setIndexName(properties.getIndexName());    // Optional: defaults to "spring-ai-document-index"
//        options.setSimilarity(properties.getSimilarity());           // Optional: defaults to COSINE
//        options.setDimensions(properties.getDimensions());             // Optional: defaults to model dimensions or 1536
//
//        return ElasticsearchVectorStore.builder(restClient, embeddingModel)
//                .options(options)                     // Optional: use custom options
//                .initializeSchema(true)               // Optional: defaults to false
//                .batchingStrategy(new TokenCountBatchingStrategy()) // Optional: defaults to TokenCountBatchingStrategy
//                .build();
//    }

//    @Bean
//    @Qualifier("redisVectorStore")
//    public RedisVectorStore redisVectorStore(
//            RedisTemplate<String, Object> redisTemplate,
//            @Value("${spring.ai.vectorstore.redis.index:research-agent-index}") String indexName) {
//
//        return new RedisVectorStore(redisTemplate, indexName);
//    }
//
//    @Bean
//    @Qualifier("milvusVectorStore")
//    public MilvusVectorStore milvusVectorStore(
//            MilvusClient milvusClient,
//            @Value("${spring.ai.milvus.collectionName:vector_store}") String collectionName,
//            @Value("${spring.ai.milvus.embeddingDimension:1536}") int embeddingDimension) {
//
//        return new MilvusVectorStore(milvusClient, collectionName, embeddingDimension);
//    }
}

