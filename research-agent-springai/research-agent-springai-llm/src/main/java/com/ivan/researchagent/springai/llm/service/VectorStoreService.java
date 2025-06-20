package com.ivan.researchagent.springai.llm.service;

import com.google.common.collect.Maps;
import com.ivan.researchagent.springai.llm.model.rag.VectorStoreData;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/4/周三
 **/
@Slf4j
@Service
public class VectorStoreService {

    @Resource
    VectorStore vectorStore;

    TokenTextSplitter tokenTextSplitter;

    @PostConstruct
    public void init() {
        tokenTextSplitter = TokenTextSplitter.builder()
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
    }

    public Boolean storeText(VectorStoreData vectorStoreData) {
        // 1.parameter verification
        if (!StringUtils.hasText(vectorStoreData.getContent())) {
            throw new IllegalArgumentException("请输入有效的内容");
        }
        // 2.parse document
        List<Document> documents = List.of(new Document(vectorStoreData.getContent()));

        // 3.Splitting Text
        List<Document> splitDocuments = new TokenTextSplitter().apply(documents);

        // 4. add metadata to each document
        if (MapUtils.isNotEmpty(vectorStoreData.getMetadata())) {
            // 3.1. add metadata to each document
            for (Document doc : splitDocuments) {
                doc.getMetadata().putAll(vectorStoreData.getMetadata());
            }
        }

        // 5.create embedding and store to vector store
        vectorStore.add(splitDocuments);
        // 6.return success prompt
        log.info("successfully inserted {} text fragments into vector store", splitDocuments.size());
        return Boolean.TRUE;
    }

    public String storeFiles(MultipartFile file) {
        try {
            // 1. file verification
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("必须上传非空的文件");
            }
            // 2. parse files
            List<Document> docs = new TikaDocumentReader(file.getResource()).get();

            // 3. Splitting Text
            List<Document> splitDocs = new TokenTextSplitter().apply(docs);

            String fileId = UUID.randomUUID().toString();
            for (Document doc : splitDocs) {
                doc.getMetadata().put("fileId", fileId);
            }

            // 4. create embedding and store to vector store
            vectorStore.add(splitDocs);

            // 5.return success prompt
            log.info("successfully inserted {} text fragments into vector store, fileId:{}", splitDocs.size(), fileId);
            return fileId;
        } catch (Exception e) {
            log.error("Error processing file upload", e);
            throw new RuntimeException("文件处理失败: " + e.getMessage());
        }
    }

    public Boolean updateData(VectorStoreData vectorStoreData) {
        // 1.parameter verification
        if (!StringUtils.hasText(vectorStoreData.getId())) {
            throw new IllegalArgumentException("请输入有效的ID");
        }
        if (!StringUtils.hasText(vectorStoreData.getContent())) {
            throw new IllegalArgumentException("请输入有效的内容");
        }

        // 2. create document with ID and content
        Document document = new Document(vectorStoreData.getId(), vectorStoreData.getContent(), vectorStoreData.getMetadata());

        // 3. add metadata to document
        List<Document> splitDocuments = new TokenTextSplitter().apply(List.of(document));

        // 4. update the document in the vector store
        vectorStore.accept(splitDocuments);
        log.info("successfully updated document with ID: {}", vectorStoreData.getId());
        return Boolean.TRUE;
    }


    public Boolean deleteData(VectorStoreData vectorStoreData) {
        // 1.parameter verification
        if (!StringUtils.hasText(vectorStoreData.getId())) {
            throw new IllegalArgumentException("请输入有效的ID");
        }

        // 2. delete the document from the vector store
        vectorStore.delete(List.of(vectorStoreData.getId()));
        log.info("successfully deleted document with ID: {}", vectorStoreData.getId());
        return Boolean.TRUE;
    }
}
