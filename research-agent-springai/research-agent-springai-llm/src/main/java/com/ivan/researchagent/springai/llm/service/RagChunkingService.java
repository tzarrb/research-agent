package com.ivan.researchagent.springai.llm.service;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentTransformer;
import com.alibaba.cloud.ai.transformer.splitter.SentenceSplitter;
import com.ivan.researchagent.common.enumerate.ChunkingTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.DefaultContentFormatter;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.transformer.ContentFormatTransformer;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description: RAG 分块优化服务
 *              常常使用5种分块技术：固定大小分块、语义分块、递归分块、基于文档结构分块、基于 LLM 分块(关键字元数据和摘要元数据)。
 * @author: ivan
 * @since: 2025/7/23/周三
 **/
@Slf4j
@Service
public class RagChunkingService {

    @Resource
    private ChatService chatService;

    @Resource
    private DashScopeChatModel chatModel;
//    @Resource
//    private DashScopeApi dashScopeApi;

    public List<Document> chunking(ChunkingTypeEnum chunkingType, List<Document> documents) {
        log.info("start chunking");
        switch (chunkingType) {
            case TOKEN_TEXT:
                return tokenTextSplitter(documents);
            case CONTENT_FORMAT:
                return contentFormatTransformer(documents);
            case KEYWORD_METADATA:
                return keywordMetadataEnricher(documents);
            case SUMMARY_METADATA:
                return summaryMetadataEnricher(documents);
            default:
                return documents;
        }
    }

    /**
     * 基于句子的分块
     * @param documents
     * @return
     */
    public List<Document> sentenceSplitter(List<Document> documents) {
        log.info("start sentence splitter");
        SentenceSplitter sentenceSplitter = new SentenceSplitter(800);
        return sentenceSplitter.split(documents);
    }
    /**
     * 基于令牌数的分块,最适合LLM处理
     * @param documents
     * @return
     */
    public List<Document> tokenTextSplitter(List<Document> documents) {
        log.info("start token text splitter");
        TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder()
                // 每个文本块的目标token数量
                .withChunkSize(800)
                // 每个文本块的最小字符数
                .withMinChunkSizeChars(350)
                // 丢弃小于此长度的文本块
                .withMinChunkLengthToEmbed(5)
                // 文本中生成的最大块数
                .withMaxNumChunks(10000)
                // 是否保留分隔符
                .withKeepSeparator(true)
                .build();
        return tokenTextSplitter.split(documents);
    }

    /**
     * 内容格式转换器，规范化文档内容
     * @param documents
     * @return
     */
    public List<Document> contentFormatTransformer(List<Document> documents) {
        log.info("start content format transformer");
        DefaultContentFormatter defaultContentFormatter = DefaultContentFormatter.defaultConfig();

        ContentFormatTransformer contentFormatTransformer = new ContentFormatTransformer(defaultContentFormatter);

        return contentFormatTransformer.apply(documents);
    }

    /**
     * 关键词元数据嵌入，提升检索和理解能力
     * @param documents
     * @return
     */
    public List<Document> keywordMetadataEnricher(List<Document> documents) {
        log.info("start keyword metadata enricher");
        // 每隔文档提取5个关键词
        KeywordMetadataEnricher keywordMetadataEnricher = new KeywordMetadataEnricher(this.chatModel, 5);
        return keywordMetadataEnricher.apply(documents);
    }

    /**
     * 摘要元数据嵌入
     * @param documents
     * @return
     */
    public List<Document> summaryMetadataEnricher(List<Document> documents) {
        log.info("start summary metadata enricher");
        List<SummaryMetadataEnricher.SummaryType> summaryTypes = List.of(
                SummaryMetadataEnricher.SummaryType.NEXT,
                SummaryMetadataEnricher.SummaryType.CURRENT,
                SummaryMetadataEnricher.SummaryType.PREVIOUS);
        SummaryMetadataEnricher summaryMetadataEnricher = new SummaryMetadataEnricher(this.chatModel, summaryTypes);

        return summaryMetadataEnricher.apply(documents);
    }

    /**
     * DashScope 文档转换器
     * @param documents
     * @return
     */
//    public List<Document> dashScopeDocumentTransformer(List<Document> documents) {
//        log.info("start dashscope document transformer");
//        DashScopeDocumentTransformer dashScopeDocumentTransformer = new DashScopeDocumentTransformer(dashScopeApi);
//        return dashScopeDocumentTransformer.apply(documents);
//    }
}
