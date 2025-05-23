package com.ivan.researchagent.main.controller;

import com.alibaba.cloud.ai.model.RerankModel;
import com.ivan.researchagent.main.model.vo.VectorStoreData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/5/14/周三
 **/
@Slf4j
@RestController
@RequestMapping("/vector")
@CrossOrigin(origins = "*") // 支持所有来源的跨域请求
@Tag(name = "向量存储控制器", description = "向量存储控制器")
public class VectorStoreController {

    @Resource
    VectorStore vectorStore;

    @Resource
    RerankModel rerankModel;

    @PostMapping("store")
    @Operation(summary = "存储", description = "存储向量数据")
    public void store(@RequestBody VectorStoreData vectorStoreData) {
        vectorStore.add(List.of(new Document(vectorStoreData.getContent(), vectorStoreData.getMetadata())));
    }

    @PostMapping("update")
    @Operation(summary = "更新", description = "更新向量数据")
    public void update(@RequestBody VectorStoreData vectorStoreData) {
        vectorStore.accept(List.of(new Document(vectorStoreData.getId(), vectorStoreData.getContent(), vectorStoreData.getMetadata())));
    }

    @PostMapping("delete")
    @Operation(summary = "删除", description = "删除向量数据")
    public void delete(@RequestBody VectorStoreData vectorStoreData) {
        vectorStore.delete(List.of(vectorStoreData.getId()));
    }

}
