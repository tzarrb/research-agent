package com.ivan.researchagent.springai.llm.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.xml.DocumentLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/20/周五
 **/
@Configuration
public class ETLPipelineConfig {

    // ETL管道配置
    public static final String PIPELINE_NAME = "etlPipeline";
    public static final String SOURCE_STEP = "sourceStep";
    public static final String TRANSFORM_STEP = "transformStep";
    public static final String LOAD_STEP = "loadStep";

    // 数据源配置
    public static final String SOURCE_TYPE_FILE = "file";
    public static final String SOURCE_TYPE_DATABASE = "database";

    // 目标存储配置
    public static final String TARGET_TYPE_FILE = "file";
    public static final String TARGET_TYPE_DATABASE = "database";

//    @Bean
//    public ETLPipeline etlPipeline(
//            DocumentLoader documentLoader,
//            EmbeddingModel embeddingModel,
//            VectorStore vectorStore) {
//        return ETLPipeline.builder()
//                .documentLoader(documentLoader)
//                .embeddingModel(embeddingModel)
//                .vectorStore(vectorStore)
//                .build();
//    }
}
