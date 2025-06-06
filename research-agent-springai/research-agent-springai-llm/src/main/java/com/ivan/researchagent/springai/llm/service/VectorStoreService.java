package com.ivan.researchagent.springai.llm.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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

    public Boolean insertText(String text) {
        // 1.parameter verification
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("请输入有效的内容");
        }
        // 2.parse document
        List<Document> documents = List.of(new Document(text));

        // 3.Splitting Text
        List<Document> splitDocuments = new TokenTextSplitter().apply(documents);

        // 4.create embedding and store to vector store
        vectorStore.add(splitDocuments);
        // 5.return success prompt
        log.info("successfully inserted {} text fragments into vector store", splitDocuments.size());
        return Boolean.TRUE;
    }

    public Boolean insertFiles(MultipartFile file) {
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
        log.info("successfully inserted {} text fragments into vector store", splitDocs.size());
        return Boolean.TRUE;
    }

}
