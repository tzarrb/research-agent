package com.ivan.researchagent.springai.llm.configuration;

import com.alibaba.cloud.ai.advisor.RetrievalRerankAdvisor;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.model.RerankModel;
import com.ivan.researchagent.springai.llm.advisors.ReasoningContentAdvisor;
import com.ivan.researchagent.springai.llm.rag.retriever.WebSearchDocumentRetriever;
import com.ivan.researchagent.springai.llm.tools.search.tavilysearch.TavilySearchApiApi;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/24/周二
 **/
@Configuration
public class RagConfiguration {

    @Resource
    private DashScopeChatModel chatModel;
    @Resource
    private VectorStore vectorStore;
    @Resource
    private RerankModel rerankModel;

    @Resource
    private TavilySearchApiApi tavilySearchApi;

    @Qualifier("queryArgumentPromptTemplate")
    private PromptTemplate queryArgumentPromptTemplate;

    @Qualifier("transformerPromptTemplate")
    private PromptTemplate transformerPromptTemplate;

    @Resource(name = "qaPromptTemplate")
    PromptTemplate qsPromptTemplate;

    private ChatClient chatClient;

    private static final String DEFAULT_WEB_SEARCH_MODEL = "deepseek-r1";
    @PostConstruct
    public void init() {
        // Build chatClient
        this.chatClient = ChatClient.builder(chatModel)
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withModel("qwen-max")
                                // stream 模式下是否开启增量输出
                                .withIncrementalOutput(true)
                                .build())
                .build();
    }

    @Bean(name = "vectorStoreDocumentRetriever")
    public VectorStoreDocumentRetriever vectorStoreDocumentRetriever() {
        // 使用VectorStoreDocumentRetriever进行文档检索
        var documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.50) // 相似度阈值
                .topK(3) // 相似度阈值
                .build();
        return documentRetriever;
    }

    @Bean(name = "webSearchDocumentRetriever")
    public WebSearchDocumentRetriever webSearchDocumentRetriever() {
        // 使用WebSearchDocumentRetriever进行文档检索
        var documentRetriever = WebSearchDocumentRetriever.builder()
                .webSearchApi(tavilySearchApi)
                .maxResults(3)
                .build();
        return documentRetriever;
    }

    @Bean(name = "multiQueryExpander")
    public MultiQueryExpander multiQueryExpander() {

        // 用于生成多个相关的查询变体，以获得更全面的搜索结果
        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(chatClient.mutate())
                .includeOriginal(false) // 不包含原始查询
                .numberOfQueries(2) // 生成2个查询变体
                .build();
        return queryExpander;
    }

    @Bean(name = "rewriteQueryTransformer")
    public RewriteQueryTransformer rewriteQueryTransformer() {
        //查询扩展器，用于生成多个相关的查询变体，以获得更全面的搜索结果
        RewriteQueryTransformer rewriteQueryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())
                .promptTemplate(transformerPromptTemplate)
                .targetSearchSystem("Local Search")
                .build();
        return rewriteQueryTransformer;
    }

    @Bean(name = "webSearchQueryTransformer")
    public RewriteQueryTransformer webSearchQueryTransformer() {
        // 查询翻译转换器，用于将查询翻译为目标语言
        return RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())
                .promptTemplate(transformerPromptTemplate)
                .targetSearchSystem("Web Search")
                .build();
    }

    @Bean(name = "englishQueryTransformer")
    public TranslationQueryTransformer englishQueryTransformer() {
        // 查询翻译转换器，用于将查询翻译为目标语言
        var translationQueryTransformer = TranslationQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())
                .targetLanguage("english")
                .build();
        return translationQueryTransformer;
    }

    @Bean(name = "compressionQueryTransformer")
    public CompressionQueryTransformer compressionQueryTransformer() {
        // 负责将含有上下文的查询转换为一个完整的独立查询
        var compressionQueryTransformer = CompressionQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())
                .build();
        return compressionQueryTransformer;
    }

    @Bean(name = "concatenationDocumentJoiner")
    public ConcatenationDocumentJoiner concatenationDocumentJoiner() {
        // 文档合并器
        ConcatenationDocumentJoiner documentJoiner = new ConcatenationDocumentJoiner();
        return documentJoiner;
    }


    @Bean(name = "contextualQueryAugmenter")
    public ContextualQueryAugmenter contextualQueryAugmenter() {
        // 边界情况处理，处理文档未找到情况：处理相似度过低情况；处理查询超时情况。
        var queryAugmenter = ContextualQueryAugmenter.builder()
                .promptTemplate(queryArgumentPromptTemplate) // 查询参数模板
                .allowEmptyContext(true)
                .build();
        return queryAugmenter;
    }

    @Bean(name = "vectorStoreRagAdvisor")
    public RetrievalAugmentationAdvisor vectorStoreRagAdvisor(VectorStoreDocumentRetriever vectorStoreDocumentRetriever,
                                                                       ConcatenationDocumentJoiner concatenationDocumentJoiner,
                                                                       ContextualQueryAugmenter contextualQueryAugmenter,
                                                                       MultiQueryExpander multiQueryExpander,
                                                                       RewriteQueryTransformer rewriteQueryTransformer,
                                                                       CompressionQueryTransformer compressionQueryTransformer) {
        //  RAG检索增强生成
        var retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(vectorStoreDocumentRetriever) // 本地文档检索
                .documentJoiner(concatenationDocumentJoiner) // 文档合并
                .queryAugmenter(contextualQueryAugmenter) //边界情况处理
                .queryExpander(multiQueryExpander) // 查询扩展器
                .queryTransformers(rewriteQueryTransformer, compressionQueryTransformer) // 查询转换器
                .build();
        return retrievalAugmentationAdvisor;
    }

    @Bean(name = "webSearchRagAdvisor")
    public RetrievalAugmentationAdvisor webSearchRagAdvisor(WebSearchDocumentRetriever webSearchDocumentRetriever,
                                                              ConcatenationDocumentJoiner concatenationDocumentJoiner,
                                                              ContextualQueryAugmenter contextualQueryAugmenter,
                                                              MultiQueryExpander multiQueryExpander,
                                                              RewriteQueryTransformer webSearchQueryTransformer,
                                                              CompressionQueryTransformer compressionQueryTransformer) {
        //  RAG检索增强生成
        var retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(webSearchDocumentRetriever) // 网络文档检索
                .documentJoiner(concatenationDocumentJoiner) // 文档合并
                .queryAugmenter(contextualQueryAugmenter) //边界情况处理
                .queryExpander(multiQueryExpander) // 查询扩展器
                .queryTransformers(webSearchQueryTransformer, compressionQueryTransformer) // 查询转换器
                .build();
        return retrievalAugmentationAdvisor;
    }

    @Bean(name = "retrievalRerankAdvisor")
    public RetrievalRerankAdvisor retrievalRerankAdvisor () {
        //使用RerankModel进行重排序
        SearchRequest searchRequest = SearchRequest.builder().topK(2).build();
        RetrievalRerankAdvisor retrievalRerankAdvisor = new RetrievalRerankAdvisor(
                vectorStore,
                rerankModel,
                searchRequest,
                qsPromptTemplate,
                0.1);
        return retrievalRerankAdvisor;
    }

    @Bean(name = "reasoningContentAdvisor")
    public ReasoningContentAdvisor reasoningContentAdvisor() {
        // 深度搜索的推理内容顾问
        ReasoningContentAdvisor reasoningContentAdvisor = new ReasoningContentAdvisor(1);
        return reasoningContentAdvisor;
    }
}
