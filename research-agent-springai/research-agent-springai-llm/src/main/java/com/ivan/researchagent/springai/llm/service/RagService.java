package com.ivan.researchagent.springai.llm.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.ivan.researchagent.springai.llm.model.chat.ChatMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.DefaultContentFormatter;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.ai.transformer.ContentFormatTransformer;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/18/周三
 **/
@Slf4j
@Service
public class RagService {

    @Resource
    private ChatService chatService;

    @Resource
    private DashScopeChatModel chatModel;

    /**
     * 多查询扩展
     * 用于生成多个相关的查询变体，进而提升搜索的精确度和覆盖率，以获得更全面的搜索结果
     *
     * @param chatMessage
     */
    public void multiQueryExpansion(ChatMessage chatMessage) {
        // 获取聊天客户端实例
        ChatClient chatClient = chatService.getchatClient(chatMessage);

        // 创建聊天客户端实例// 设置系统提示信息，定义AI助手作为专业的室内设计顾问角色
//        ChatClient chatClient = builder.defaultSystem("你是一位专业的室内设计顾问，精通各种装修风格、材料选择和空间布局。请基于提供的参考资料，为用户提供专业、详细且实用的建议。在回答时，请注意：\n"+
//                "1. 准确理解用户的具体需求\n" +
//                "2. 结合参考资料中的实际案例\n" +
//                "3. 提供专业的设计理念和原理解释\n" +
//                "4. 考虑实用性、美观性和成本效益\n" +
//                "5. 如有需要，可以提供替代方案")
//                .build();
        // 构建查询扩展器
        // 用于生成多个相关的查询变体，以获得更全面的搜索结果
        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(chatClient.mutate())
                .includeOriginal(false) // 不包含原始查询
                .numberOfQueries(3) // 生成3个查询变体
                .build();

        // 执行查询扩展
        // 将原始问题"请提供几种推荐的装修风格?"扩展成多个相关查询
        List<Query> queries = queryExpander.expand(new Query("请提供几种推荐的装修风格?"));
        System.out.println(queries);
    }

    /**
     * 查询重写
     * 查询改写是 RAG 系统中的一项关键优化手段，它通过将用户的原始查询转化为更加规范和明确的查询形式，从而提升搜索的精确度，并协助系统更准确地把握用户的真正需求。
     *
     * @param chatMessage
     */
    public void queryRewrite(ChatMessage chatMessage) {
        // 创建聊天客户端实例
        ChatClient chatClient = chatService.getchatClient(chatMessage);

        // 构建一个模拟用户在学习人工智能过程中的查询场景
        Query query = new Query("我在学习人工智能，能否解释一下什么是大型语言模型？");

        // 实例化查询改写转换器
        QueryTransformer queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())// 假设builder是之前定义好的ChatClient构建器
                .build();
        // 执行查询改写操作
        Query transformedQuery = queryTransformer.transform(query);

        // 打印改写后的查询内容
        System.out.println(transformedQuery.text());
    }

    /**
     * 查询翻译
     * 查询翻译是 RAG 系统中的一项便捷功能，它允许将用户的查询从一个语言版本转换为另一个语言版本。这项功能对于实现多语言支持和执行跨语言搜索查询尤其重要。
     *
     * @param chatMessage
     */
    public void queryTranslation(ChatMessage chatMessage) {
        // 创建聊天客户端实例
        ChatClient chatClient = chatService.getchatClient(chatMessage);

        // 初始化一个英文的查询实例
        Query query = new Query("What is LLM?");
        // 实例化查询翻译转换器，并指定目标语言为中文
        QueryTransformer queryTransformer = TranslationQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())// 假设builder是已定义的聊天客户端构建器
                .targetLanguage("zh") // 设置目标语言代码为中文
                .build();
        // 执行查询的翻译操作
        Query translatedQuery = queryTransformer.transform(query);

        // 打印出翻译后的查询内容
        System.out.println(translatedQuery.text());
    }

    /**
     * 上下文感知查询
     * 上下文感知查询是 RAG 系统中的一项高级功能，它允许系统在处理用户查询时，考虑到之前的对话历史和上下文信息。这种能力使得系统能够更准确地理解用户的意图，并提供更相关的答案。
     *
     * @param chatMessage
     */
    public void contextAwareQueries(ChatMessage chatMessage) {
        // 创建聊天客户端实例
        ChatClient chatClient = chatService.getchatClient(chatMessage);

        // 创建一个包含历史对话的查询实例
        // 这个示例模拟了一个用户咨询房地产的场景，用户首先询问了小区的位置，随后询问房价
        Query query =Query.builder().text("那么这个小区的二手房平均价格是多少？")// 用户当前的问题
                .history(new UserMessage("深圳市南山区的碧海湾小区具体位置是？"), // 用户之前的问题
                    new AssistantMessage("碧海湾小区坐落于深圳市南山区后海中心区，靠近后海地铁站。")) // 系统之前的答复
                .build();

        // 初始化查询转换器
        // QueryTransformer负责将含有上下文的查询转换为一个完整的独立查询
        QueryTransformer queryTransformer = CompressionQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())// 假设builder是之前定义好的聊天客户端构建器
                .build();
        // 执行查询转换操作
        // 将不明确的指代（“这个小区”）转换为具体的实体名称（“碧海湾小区”）
        Query transformedQuery = queryTransformer.transform(query);

        // 打印转换后的查询内容
        System.out.println(transformedQuery.text());
    }

    /**
     * 文档合并
     * 文档合并是 RAG 系统中的一项重要功能，它允许将来自多个查询或数据源的文档集合进行合并，以便为用户提供更全面和一致的信息。
     *
     * @param chatMessage
     */
    public void documentJoiner(ChatMessage chatMessage) {
        // 创建聊天客户端实例
        ChatClient chatClient = chatService.getchatClient(chatMessage);

        // 获取来自多个查询或数据源的文档集合
        Map<Query,List<List<Document>>> documentsMap = new HashMap<>();

        // 实例化文档合并器
        DocumentJoiner documentJoiner = new ConcatenationDocumentJoiner();
        // 执行文档合并操作
        List<Document> mergedDocuments = documentJoiner.join(documentsMap);
    }


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

    public List<Document> contentFormatTransformer(List<Document> documents) {
        log.info("start content format transformer");
        DefaultContentFormatter defaultContentFormatter = DefaultContentFormatter.defaultConfig();

        ContentFormatTransformer contentFormatTransformer = new ContentFormatTransformer(defaultContentFormatter);

        return contentFormatTransformer.apply(documents);
    }

    public List<Document> keywordMetadataEnricher(List<Document> documents) {
        log.info("start keyword metadata enricher");
        KeywordMetadataEnricher keywordMetadataEnricher = new KeywordMetadataEnricher(this.chatModel, 3);
        return keywordMetadataEnricher.apply(documents);
    }

    @GetMapping("/summary-metadata-enricher")
    public List<Document> summaryMetadataEnricher(List<Document> documents) {
        log.info("start summary metadata enricher");
        List<SummaryMetadataEnricher.SummaryType> summaryTypes = List.of(
                SummaryMetadataEnricher.SummaryType.NEXT,
                SummaryMetadataEnricher.SummaryType.CURRENT,
                SummaryMetadataEnricher.SummaryType.PREVIOUS);
        SummaryMetadataEnricher summaryMetadataEnricher = new SummaryMetadataEnricher(this.chatModel, summaryTypes);

        return summaryMetadataEnricher.apply(documents);
    }
}