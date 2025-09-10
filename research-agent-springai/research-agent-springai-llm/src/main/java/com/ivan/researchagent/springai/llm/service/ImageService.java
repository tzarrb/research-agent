package com.ivan.researchagent.springai.llm.service;

import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.ivan.researchagent.springai.llm.model.image.ImageRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.image.*;
import org.springframework.stereotype.Service;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/09 17:17
 **/
@Slf4j
@Service
public class ImageService {

    @Resource
    private DashScopeImageModel imageModel;

    private static final int MAX_RETRIES = 60;
    private static final long POLL_INTERVAL_MS = 10*1000;
    private static final long TIMEOUT_MS = 5*60*1000;

    public String genImage(ImageRequest imageRequest) {
        if (StringUtils.isBlank(imageRequest.getPrompt())) {
            return "";
        }

        ImageOptionsBuilder optionsBuilder = ImageOptionsBuilder.builder()
                //.model(DashScopeImageApi.ImageModel.WANX2_1_T2I_PLUS.getValue())
                .N(imageRequest.getNumber());
        if (StringUtils.isNotBlank(imageRequest.getResolution())) {
            optionsBuilder
                    .width(Integer.valueOf(imageRequest.getResolution().split("\\*")[0]))
                    .height(Integer.valueOf(imageRequest.getResolution().split("\\*")[1]));
        }
        if (StringUtils.isNotBlank(imageRequest.getStyle())) {
            optionsBuilder.style(imageRequest.getStyle());
        }

        ImagePrompt imagePrompt = new ImagePrompt(imageRequest.getPrompt(), optionsBuilder.build());
        try {
            ImageGeneration result = pollForImageResult(imagePrompt);
            return result.getOutput().getUrl();
        } catch (Exception e) {
            log.error("Image generation failed", e);
            return "Error: " + e.getMessage();
        }
    }

    private ImageGeneration pollForImageResult(ImagePrompt imagePrompt) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        int retryCount = 0;

        while (retryCount < MAX_RETRIES && (System.currentTimeMillis() - startTime) < TIMEOUT_MS) {
            try {
                ImageGeneration result = imageModel.call(imagePrompt).getResult();
                if (result != null && result.getOutput() != null) {
                    return result;
                }
            } catch (RuntimeException e) {
                if (!e.getMessage().contains("still pending")) {
                    throw e;
                }
                log.info("Image generation pending, retrying...");
            }

            Thread.sleep(POLL_INTERVAL_MS);
            retryCount++;
        }

        throw new RuntimeException("Image generation timed out after " + TIMEOUT_MS + "ms");
    }
}
