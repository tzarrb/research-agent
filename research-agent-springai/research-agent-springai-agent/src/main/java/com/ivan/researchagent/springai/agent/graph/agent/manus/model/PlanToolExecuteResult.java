package com.ivan.researchagent.springai.agent.graph.agent.manus.model;

import com.ivan.researchagent.springai.agent.graph.agent.manus.tool.Plan;
import com.ivan.researchagent.springai.llm.model.tool.ToolExecuteResult;
import lombok.Data;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/21/周四
 **/
@Data
public class PlanToolExecuteResult extends ToolExecuteResult {

    private String id;

    private Plan plan;

    public PlanToolExecuteResult(String output, String id) {
        super(output);
        this.id = id;
    }

    public PlanToolExecuteResult(Plan plan, String output, String id) {
        super(output);
        this.id = id;
        this.plan = plan;
    }

}
