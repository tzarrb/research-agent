package com.ivan.researchagent.springai.agent.model.tool;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @author: ivan
 * @since: 2025/1/5 1:50
 */
@Data
public class AgentRequest {
    /**
     * 用户原始输入
     */
    String originalInput;

    /**
     * 目标agent名称
     */
    String targetAgent;

    /**
     * 任务类别
     */
    String category;

    /**
     * 优先级(1-5)
     */
    String priority;

    /**
     * 时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    Date timestamp;
}
