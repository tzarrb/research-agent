package com.ivan.researchagent.springai.agent.agentic.chat;

import com.ivan.researchagent.springai.llm.model.ChatMessage;
import com.ivan.researchagent.springai.llm.model.ChatResult;
import reactor.core.publisher.Flux;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/24 16:42
 **/
public interface IBaseChat {
    ChatResult call(ChatMessage chatMessage);

    Flux<ChatResult> stream(ChatMessage chatMessage);
}
