package com.ivan.researchagent.springai.llm.advisors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/25 09:57
 **/
@Slf4j
public class LoggingAdvisor extends SimpleLoggerAdvisor {

//    @Override
//    public AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
//        String conversationId = (String) context.get(CHAT_MEMORY_CONVERSATION_ID_KEY);
//        log.info("conversationId:{}, Request:{}", conversationId, request);
//        return request;
//    }
//
//    @Override
//    public ChatResponse adviseResponse(ChatResponse response, Map<String, Object> context) {
//        String conversationId = (String) context.get(CHAT_MEMORY_CONVERSATION_ID_KEY);
//        log.info("conversationId:{}, Response:{}", conversationId, response.getResult().getOutput().getContent());
//        return response;
//    }

//    @Override
//    public int getOrder() {
//        return 0;
//    }
}
