package com.ivan.researchagent.springai.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/16 11:35
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResult {
    /**
     * 大模型回复内容
     */
    private String content;

    /**
     * 对话会话ID，也是对话记忆的唯一标识
     */
    private String sessionId;

    /**
     * 大模型回复信息
     */
    private ChatResponse chatResponse;

}
