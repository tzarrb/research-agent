package com.ivan.researchagent.springai.llm.model.tool;

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
public class ToolExecuteResult {

    public ToolExecuteResult() {

    }

    public ToolExecuteResult(String output) {
        setOutput(output);
    }

    public ToolExecuteResult(String output, boolean interrupted) {
        setOutput(output);
        setInterrupted(interrupted);
    }

    /**
     * 工具返回的内容
     */
    private String output;

    /**
     * 是否中断
     */
    private boolean interrupted;

}
