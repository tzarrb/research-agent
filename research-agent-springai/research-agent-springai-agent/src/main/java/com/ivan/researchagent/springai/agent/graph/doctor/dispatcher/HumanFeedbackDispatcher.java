package com.ivan.researchagent.springai.agent.graph.doctor.dispatcher;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.EdgeAction;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/7/3/周四
 **/
public class HumanFeedbackDispatcher implements EdgeAction {
    @Override
    public String apply(OverAllState state) throws Exception {
        return state.value("human_next_node", StateGraph.END);
    }
}
