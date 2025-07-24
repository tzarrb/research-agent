package com.ivan.researchagent.main.controller;

import com.ivan.researchagent.springai.llm.model.rag.VectorStoreData;
import com.ivan.researchagent.springai.llm.service.VectorStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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
    VectorStoreService vectorStoreService;

    @PostMapping("/store")
    @Operation(summary = "存储", description = "存储向量数据")
    public void store(@RequestBody VectorStoreData vectorStoreData) {
        vectorStoreService.storeData(vectorStoreData);
    }

    @PostMapping("/update")
    @Operation(summary = "更新", description = "更新向量数据")
    public void update(@RequestBody VectorStoreData vectorStoreData) {
        vectorStoreService.updateData(vectorStoreData);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除", description = "删除向量数据")
    public void delete(@RequestBody VectorStoreData vectorStoreData) {
        vectorStoreService.deleteData(vectorStoreData);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> insertFiles(@RequestPart(value = "file", required = false) MultipartFile file) {
        String msg = vectorStoreService.storeFile(file);
        return ResponseEntity.ok(msg);
    }

    @PostMapping(value = "/batch-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<String>> batchInsertFiles(@RequestPart(value = "files", required = false) List<MultipartFile> files) {
        List<String> fileNames = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String fileName = vectorStoreService.storeFile(file);
                fileNames.add(fileName);
            }
        }
        return ResponseEntity.ok(fileNames);
    }
}
