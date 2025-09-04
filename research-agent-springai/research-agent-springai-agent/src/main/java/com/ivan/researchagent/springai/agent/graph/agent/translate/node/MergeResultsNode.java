package com.ivan.researchagent.springai.agent.graph.agent.translate.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;

import java.util.Map;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/12/周二
 **/
public class MergeResultsNode implements NodeAction {
    @Override
    public Map<String, Object> apply(OverAllState state) {
        Object expanderContent = state.value("expander_content").orElse("unknown");
        String translateContent = (String) state.value("translate_content").orElse("");

        return Map.of("merge_result", Map.of("expander_content", expanderContent,
                "translate_content", translateContent));
    }
}
