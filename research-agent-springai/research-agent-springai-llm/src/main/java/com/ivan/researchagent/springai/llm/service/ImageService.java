package com.ivan.researchagent.springai.llm.service;

import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.ivan.researchagent.springai.llm.model.image.ImageRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.image.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

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

    /**
     * 生成图片
     * @param imageRequest
     * @return
     */
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
            if (result == null || result.getOutput() == null) {
                return "Error: Image generation returned null result";
            }
            return result.getOutput().getUrl();
        } catch (Exception e) {
            log.error("Image generation failed", e);
            return "Error: " + e.getMessage();
        }
    }

    private ImageGeneration pollForImageResult(ImagePrompt prompt) {
        return Mono.defer(() -> {
                    ImageGeneration result = imageModel.call(prompt).getResult();
                    if (result != null && result.getOutput() != null) {
                        return Mono.just(result);
                    }
                    throw new RuntimeException("Image generation result is null");
                })
                .retryWhen(Retry.fixedDelay(MAX_RETRIES, Duration.ofMillis(POLL_INTERVAL_MS))
                        // only retry on the “still pending” case
                        .filter(ex -> ex.getMessage().contains("still pending")
                                || (ex instanceof RuntimeException && ex.getMessage().contains("null")))
                )
                .doOnError(ex -> log.error("Image generation failed", ex))
                .timeout(Duration.ofMillis(TIMEOUT_MS))
                .block();
    }

}
