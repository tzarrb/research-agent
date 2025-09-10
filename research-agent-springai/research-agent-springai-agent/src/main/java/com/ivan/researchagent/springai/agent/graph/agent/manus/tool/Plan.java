package com.ivan.researchagent.springai.agent.graph.agent.manus.tool;

import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/21/周四
 **/
public class Plan {

    private Map<String, String> stepStatus;

    private int currentStep = 0;

    private String task;

    private String plan_id;

    private List<String> steps;

    public Plan(String task, String planId, List<String> steps) {
        this.task = task;
        this.plan_id = planId;
        this.steps = steps;
        this.stepStatus = new HashMap<>();
    }

    public String getCurrentStep() {
        return String.valueOf(currentStep);
    }

    public String getPlan_id() {
        return plan_id;
    }

    public void updateStepStatus(String stepIndex, String status) {
        stepStatus.put(stepIndex, status);
    }

    public String nextStepPrompt() {
        String nextStepDescription = steps.get(currentStep);
        Map<String, Object> context = new HashMap<>();
        context.put("task", task);
        context.put("planWithSteps", steps);
        context.put("stepIndex", currentStep);
        context.put("nextStepDescription", nextStepDescription);
        context.put("stepStatus", stepStatus);

        currentStep++;

        String template = """
				The task is: {task}

				You are asked to follow the following plan with specific sequential steps to complete this task:
				{planWithSteps}

				You are currently at step {stepIndex} of the plan, which is: {nextStepDescription}.

				Below are the result of the previous steps, which you can use as the context to help you complete the current step:
				  {stepStatus}

				""";
        PromptTemplate promptTemplate = new PromptTemplate(template);
        return promptTemplate.render(context);
    }

    public String nextStep() {
        return steps.get(currentStep++);
    }

    public boolean isFinished() {
        return currentStep == steps.size();
    }

    void setPlan_id(String planId) {
        this.plan_id = planId;
    }

    void setSteps(List<String> steps) {
        this.steps = steps;
    }

}
