package com.ivan.researchagent.springai.llm.service;

import com.alibaba.cloud.ai.dashscope.video.DashScopeVideoModel;
import com.alibaba.cloud.ai.dashscope.video.DashScopeVideoOptions;
import com.alibaba.cloud.ai.dashscope.video.VideoPrompt;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/9/9/周二
 **/
@Slf4j
@Service
public class VideoService {

    @Resource
    private DashScopeVideoModel videoModel;

    public String genVideo(String prompt) {
        if (StringUtils.isBlank(prompt)) {
            return "";
        }

        DashScopeVideoOptions options = DashScopeVideoOptions.builder()
                //.model("")
                //.resolution("1920x1080") //画面分辨率
                .size("1280720") //大小参数应直接设置为目标分辨率的具体值（如1280720），而不是宽高比（如1：1）或分辨率齿轮名称（如480P或720P）
                .seed(0L) //随机数种子用于控制模型生成的内容的随机性。值范围为 [0， 2147483647]
                .build();

        return this.videoModel.call(new VideoPrompt(prompt))
                .getResult()
                .getOutput()
                .getVideoUrl();
    }
}
