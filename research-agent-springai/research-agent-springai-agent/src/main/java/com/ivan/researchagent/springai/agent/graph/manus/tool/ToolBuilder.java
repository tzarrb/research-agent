package com.ivan.researchagent.springai.agent.graph.manus.tool;

import com.ivan.researchagent.springai.llm.tools.browser.BrowserUseTool;
import com.ivan.researchagent.springai.llm.tools.execute.PythonExecuteTool;
import com.ivan.researchagent.springai.llm.tools.file.FileSaverTool;
import com.ivan.researchagent.springai.llm.tools.search.serpapi.GoogleSearchTool;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/21/周四
 **/
public class ToolBuilder {

    public static List<ToolCallback> getPlanningToolCalls() {
        return List.of(PlanningTool.getFunctionToolCallback());
    }

    public static List<ToolCallback> getManusAgentToolCalls() {
        return List.of(GoogleSearchTool.getFunctionToolCallback(), BrowserUseTool.getFunctionToolCallback(),
                FileSaverTool.getFunctionToolCallback(), PythonExecuteTool.getFunctionToolCallback());
    }

}
