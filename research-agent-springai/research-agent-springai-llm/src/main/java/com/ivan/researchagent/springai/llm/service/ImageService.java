package com.ivan.researchagent.springai.llm.service;

import com.alibaba.cloud.ai.dashscope.api.DashScopeImageApi;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
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
    private DashScopeImageModel dashScopeImageModel;

    public String image(String input) {
        ImageOptions options = ImageOptionsBuilder.builder()
                .model(DashScopeImageApi.ImageModel.WANX_V1.getValue())
                .N(4)
                .height(1024)
                .width(1024)
                .build();

        ImagePrompt imagePrompt = new ImagePrompt(input, options);
        ImageResponse response = dashScopeImageModel.call(imagePrompt);
        String imageUrl = response.getResult().getOutput().getUrl();
        return imageUrl;
    }
}
