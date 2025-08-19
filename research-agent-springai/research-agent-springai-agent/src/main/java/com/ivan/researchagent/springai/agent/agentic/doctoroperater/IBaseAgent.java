package com.ivan.researchagent.springai.agent.agentic.doctoroperater;

import com.ivan.researchagent.springai.llm.model.chat.ChatRequest;
import com.ivan.researchagent.springai.llm.model.chat.ChatResult;
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
public interface IBaseAgent {
    ChatResult call(ChatRequest chatRequest);

    Flux<ChatResult> stream(ChatRequest chatRequest);
}
