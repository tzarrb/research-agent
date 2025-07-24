package com.ivan.researchagent.springai.llm.service;

import com.ivan.researchagent.common.enumerate.ChunkingTypeEnum;
import com.ivan.researchagent.springai.llm.model.rag.VectorStoreData;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.writer.FileDocumentWriter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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

    @Resource
    RagChunkingService ragChunkingService;

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

    public Boolean storeData(VectorStoreData vectorStoreData) {
        // parameter verification
        if (!StringUtils.hasText(vectorStoreData.getContent()) && vectorStoreData.getResource() == null) {
            throw new IllegalArgumentException("请输入有效的内容");
        }
        // parse document
        List<Document> documents;
        if (vectorStoreData.getResource() != null) {
            documents = new TikaDocumentReader(vectorStoreData.getResource()).get();
        } else {
            documents = List.of(new Document(vectorStoreData.getContent()));
        }

        // Splitting Text
        ChunkingTypeEnum chunkingType = Optional.ofNullable(vectorStoreData.getChunkingType()).orElse(ChunkingTypeEnum.TOKEN_TEXT);
        List<Document> splitDocuments = ragChunkingService.chunking(chunkingType, documents);

        // content augmented 内容增强,增加文档的摘要元数据
        splitDocuments = ragChunkingService.summaryMetadataEnricher(splitDocuments);

        // add metadata to each document
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

    public String storeFile(MultipartFile file) {
        try {
            // file verification 文件校验
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("必须上传非空的文件");
            }
            // parse files 穿换成文档列表
//            // 创建临时文件
//            Path tempFile = Files.createTempFile("upload_", "_" + file.getOriginalFilename());
//            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
//            String fileName = file.getOriginalFilename();
//            List<Document> documents;
//            if (fileName.toLowerCase().endsWith(".pdf")) {
//                // 使用PDF读取器
//                PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(tempFile.toUri().toString());
//                documents = pdfReader.get();
//                log.info("使用PDF读取器处理文件: {}", fileName);
//            } else {
//                // 使用Tika读取器处理其他类型文件
//                TikaDocumentReader tikaReader = new TikaDocumentReader(tempFile.toUri().toString());
//                documents = tikaReader.get();
//                log.info("使用Tika读取器处理文件: {}", fileName);
//            }
//
//            // 清理临时文件
//            Files.deleteIfExists(tempFile);

            List<Document> docs = new TikaDocumentReader(file.getResource()).get();

            // Splitting Text 文档分割
            List<Document> splitDocs = ragChunkingService.tokenTextSplitter(docs);

            // content augmented 内容增强,增加文档的关键字元数据
            splitDocs = ragChunkingService.keywordMetadataEnricher(splitDocs);

            // add metadata to each document
            String fileId = UUID.randomUUID().toString();
            for (Document doc : splitDocs) {
                doc.getMetadata().put("fileId", fileId);
            }

            // write processed documents to disk 保存原始处理结果到文件
            String outputDir = "data/output/";
            String processedFileName = "chunk_"+ new Random().nextInt(100000) + "_"+file.getOriginalFilename()+".txt";
            String processedFile = outputDir + processedFileName;
            // 确保输出目录存在
            try {
                Path outputPath = Paths.get(outputDir);
                if (!Files.exists(outputPath)) {
                    Files.createDirectories(outputPath);
                }
            } catch (IOException e) {
                log.warn("无法创建输出目录: {}, 将使用当前目录", outputDir);
                processedFile = processedFileName;
            }
            new FileDocumentWriter(processedFile, true, MetadataMode.ALL, false).accept(splitDocs);

            // create embedding and store to vector store
            vectorStore.add(splitDocs);

            // return success prompt
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
        List<Document> splitDocuments = ragChunkingService.chunking(ChunkingTypeEnum.TOKEN_TEXT, List.of(document));

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
