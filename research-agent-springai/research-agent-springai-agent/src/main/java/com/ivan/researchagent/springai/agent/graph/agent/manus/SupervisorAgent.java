package com.ivan.researchagent.springai.agent.graph.agent.manus;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.fastjson.JSON;
import com.ivan.researchagent.springai.agent.graph.agent.manus.tool.Plan;
import com.ivan.researchagent.springai.agent.graph.agent.manus.tool.PlanningTool;

import java.util.Map;
import java.util.Optional;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * 协调者智能体
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/21/周四
 **/
public class SupervisorAgent implements NodeAction {

    private final PlanningTool planningTool;

    public SupervisorAgent(PlanningTool planningTool) {
        this.planningTool = planningTool;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {

        String planStr = (String) state.value("plan").orElseThrow();
        Plan tempPlan = parsePlan(planStr);
        Plan plan = planningTool.getGraphPlan(tempPlan.getPlan_id());

        Optional<Object> optionalOutput = state.value("step_output");

        if (optionalOutput.isPresent()) {
            String finalStepOutput = String.format("This is the final output of step %s:\n %s", plan.getCurrentStep(),
                    optionalOutput.get());
            plan.updateStepStatus(plan.getCurrentStep(), finalStepOutput);
        }

        String promptForNextStep;
        if (!plan.isFinished()) {
            promptForNextStep = plan.nextStepPrompt();
        }
        else {
            promptForNextStep = "Plan completed.";
        }

        return Map.of("step_prompt", promptForNextStep);
    }

    public String think(OverAllState state) {

        String nextPrompt = (String) state.value("step_prompt").orElseThrow();

        if (nextPrompt.equalsIgnoreCase("Plan completed.")) {
            state.updateState(Map.of("final_output", state.value("step_output").orElseThrow()));
            return "end";
        }

        return "continue";
    }

    private Plan parsePlan(String planJson) {
        planJson = removeMarkdownCodeBlockSyntax(planJson);
        return JSON.parseObject(planJson, Plan.class);
    }

    /**
     * 移除字符串中的Markdown代码块标记（```json 和 ```） 如果字符串不包含这些标记，则返回原始字符串
     * @param input 可能包含Markdown代码块标记的字符串
     * @return 去除了代码块标记的字符串
     */
    public static String removeMarkdownCodeBlockSyntax(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // 去除开头的 ```json 或 ```任何语言
        String result = input.trim();
        if (result.startsWith("```")) {
            int firstLineEnd = result.indexOf('\n');
            if (firstLineEnd != -1) {
                result = result.substring(firstLineEnd).trim();
            }
        }

        // 去除结尾的 ```
        if (result.endsWith("```")) {
            result = result.substring(0, result.length() - 3).trim();
        }

        return result;
    }

}
