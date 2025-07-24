package com.ivan.researchagent.architectagent.springai.rag;

import com.ivan.researchagent.main.AgentApplication;
import com.ivan.researchagent.springai.llm.model.rag.VectorStoreData;
import com.ivan.researchagent.springai.llm.service.RagChunkingService;
import com.ivan.researchagent.springai.llm.service.VectorStoreService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/4/10/周四
 **/
//@RunWith(SpringJUnit4ClassRunner.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = AgentApplication.class)
public class VectorStoreTest {
    @Autowired
    VectorStore vectorStore;
    @Resource
    RagChunkingService ragChunkingService;
    @Resource
    VectorStoreService vectorStoreService;

    @Value("classpath:/docs/23种设计模式整理.pdf")
    private org.springframework.core.io.Resource designPatternsResource;

    @Test
    public void test() {
        List<Document> documents = List.of(
                new Document("我最喜欢吃西梅，因为西梅甜分高，口感好，而且耐储存，现在中国新疆也能生产西梅了，而且不比国外的差，新疆真是个好地方，水美山美人美，地大物博", Map.of("meta1", "meta1")),
                new Document("手机我喜欢苹果，因为苹果手机更加易用和安全，操作更方便，就是价格有点贵"),
                new Document("RAG（Retrieval Augmented Generation，检索增强生成）是一种结合信息检索和文本生成的技术范式。RAG技术就像给AI装上了「实时百科大脑」，通过先查资料后回答的机制，让AI摆脱传统模型的”知识遗忘”困境。", Map.of("meta2", "meta2")));

        // Add the documents to PGVector
        //vectorStore.add(documents);

        // Update the document to PGVector
        //vectorStore.accept(List.of(new Document("df1679f4-1e94-4c7a-b920-7915500bcc5a","我最喜欢吃西梅，因为西梅甜分高，口感好，而且耐储存，现在中国新疆也能生产西梅了，而且不比智利、澳大利亚等国外的品质差，新疆真是个好地方，水美山美人美，地大物博",Map.of("meta1", "meta1"))));

//        List<Document> documents1 = List.of(new Document("陈晨，江湖人送外号陈大聪明，因长的漂亮，头大脑子聪明而闻名于江湖，擅于吃喝玩乐,", Map.of("meta1", "meta1")));
//        List<Document> splitDocuments = new TokenTextSplitter().apply(documents1);
//        vectorStore.add(splitDocuments);
//
//        // Retrieve documents similar to a query
//        List<Document> results = this.vectorStore.similaritySearch(SearchRequest.builder().query("陈晨是谁？").topK(1).build());
//        System.out.println(results);
        chunking();
    }

    @Test
    public void chunking(){
        VectorStoreData vectorStoreData = VectorStoreData.builder().resource(designPatternsResource).build();
        //List<Document> documents = ragChunkingService.chunking(ChunkingTypeEnum.TOKEN_TEXT, vectorStoreData);
        boolean result = vectorStoreService.storeData(vectorStoreData);
        System.out.println(result);
    }
}
