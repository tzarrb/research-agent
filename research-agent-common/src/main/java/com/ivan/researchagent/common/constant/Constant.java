package com.ivan.researchagent.common.constant;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/25 10:18
 **/
public interface Constant {
    // 对话记忆的唯一标识
    String CONVERSANT_ID = "conversantId";

    // 前端会话的唯一标识
    String SESSION_ID = "sessionId";

    // 聊天客户端
    String CHAT_CLIENT = "chatClient";

    // 对话记忆
    String CHAT_MEMORY = "chatMemory";

    // 聊天服务
    String CHAT_SERVICE = "chatService";

    // 对话信息
    String CHAT_MESSAGE = "chatMessage";

    // 用户输入的原始信息
    String ORIGINAL_INPUT = "originalInput";

    String KEY_CHAT_MEMORY = "researchagent:chat:memory:%s";
}
