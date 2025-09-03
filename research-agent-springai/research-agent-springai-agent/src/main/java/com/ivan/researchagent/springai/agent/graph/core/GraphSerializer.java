package com.ivan.researchagent.springai.agent.graph.core;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.serializer.plain_text.jackson.JacksonStateSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/9/1/周一
 **/
public class GraphSerializer  extends JacksonStateSerializer {

    /**
     * Instantiates a new Jackson serializer.
     */
    public GraphSerializer() {
        super(OverAllState::new);
    }

    /**
     * Gets object mapper.
     * @return the object mapper
     */
    ObjectMapper getObjectMapper() {
        return objectMapper;
    }

}
