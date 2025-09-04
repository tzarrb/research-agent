package com.ivan.researchagent.springai.agent.graph.manus.tool;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.ivan.researchagent.springai.agent.graph.manus.core.PlanStateEnum;
import com.ivan.researchagent.springai.agent.graph.manus.model.PlanToolExecuteResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * 计划制定工具
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/21/周四
 **/
@Slf4j
public class PlanningTool implements BiFunction<PlanningTool.Request, ToolContext, String> {

    private static final String name = "planning";

    public static final String description = "A planning tool that allows the agent to create and manage plans for solving complex tasks.\n"
            + "The tool provides functionality for creating plans, updating plan steps, and tracking progress.";

    public static final String PARAMETERS = """
			{
			    "type": "object",
			    "properties": {
			        "command": {
			            "description": "The command to execute. Available commands: create, update, list, get, set_active, mark_step, delete.",
			            "enum": ["create"],
			            "type": "string"
			        },
			        "plan_id": {
			            "description": "Unique identifier for the plan. Required for create, update, set_active, and delete commands. Optional for get and mark_step (uses active plan if not specified).",
			            "type": "string"
			        },
			        "title": {
			            "description": "Title for the plan. Required for create command, optional for update command.",
			            "type": "string"
			        },
			        "steps": {
			            "description": "List of plan steps. Required for create command, optional for update command.",
			            "type": "array",
			            "items": {
			                "type": "string"
			            }
			        },
			        "step_index": {
			            "description": "Index of the step to update (0-based). Required for mark_step command.",
			            "type": "integer"
			        },
			        "step_status": {
			            "description": "Status to set for a step. Used with mark_step command.",
			            "enum": ["not_started", "in_progress", "completed", "blocked"],
			            "type": "string"
			        },
			        "step_notes": {
			            "description": "Additional notes for a step. Optional for mark_step command.",
			            "type": "string"
			        }
			    },
			    "required": ["command"],
			    "additionalProperties": false
			}
			""";

    public static final PlanningTool INSTANCE = new PlanningTool();

    private Map<String, Map<String, Object>> plans = new HashMap<>();

    private Map<String, Plan> graphPlans = new HashMap<>();

    private String currentPlanId;

	public static OpenAiApi.FunctionTool getToolDefinition() {
		OpenAiApi.FunctionTool.Function function = new OpenAiApi.FunctionTool.Function(description, name, PARAMETERS);
		OpenAiApi.FunctionTool functionTool = new OpenAiApi.FunctionTool(function);
		return functionTool;
	}

	/**
	 * 获取函数工具回调实例
	 *JsonSchemaGenerator.generateForType(Request.class)
	 * @return FunctionToolCallback 函数工具回调实例，包含工具的名称、描述、输入模式等配置信息
	 */
	public static FunctionToolCallback getFunctionToolCallback() {
		// 构建函数工具回调实例，设置工具名称、实例、描述、输入模式和元数据信息
		return FunctionToolCallback.builder(name, PlanningTool.INSTANCE)
				.description(description)
				.inputSchema(PARAMETERS)
				.inputType(Request.class)
				.toolMetadata(ToolMetadata.builder().returnDirect(true).build())
				.build();
	}

	/**
	 * 执行工具逻辑
	 *
	 * @param request 工具输入参数
	 * @param context   工具执行环境
	 * @return 工具执行结果
	 */
	public String run(Request request, ToolContext context) {
		try {
			log.info("PlanningTool toolInput:{}", request);
//			Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {
//			});

//			String command = null;
//			if (toolInputMap.get("command") != null) {
//				command = (String) toolInputMap.get("command");
//			}
//			String planId = null;
//			if (toolInputMap.get("plan_id") != null) {
//				planId = (String) toolInputMap.get("plan_id");
//			}
//			else {
//				planId = "G_" + UUID.randomUUID().toString();
//			}
//			String title = null;
//			if (toolInputMap.get("title") != null) {
//				title = (String) toolInputMap.get("title");
//			}
//			List<String> steps = null;
//			if (toolInputMap.get("steps") != null) {
//				steps = (List<String>) toolInputMap.get("steps");
//			}
//			Integer stepIndex = null;
//			if (toolInputMap.get("step_index") != null) {
//				stepIndex = (Integer) toolInputMap.get("step_index");
//			}
//			String stepStatus = null;
//			if (toolInputMap.get("step_status") != null) {
//				stepStatus = (String) toolInputMap.get("step_status");
//			}
//			String stepNotes = null;
//			if (toolInputMap.get("step_notes") != null) {
//				stepNotes = (String) toolInputMap.get("step_notes");
//			}

			switch (request.command) {
				case "create":
					return createPlan(request.plan_id, request.title, request.steps, context);
				default:
					throw new RuntimeException("Unrecognized command: " + request.command
							+ ". Allowed commands are: create, update, list, get, set_active, mark_step, delete");
			}
		}
		catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 创建计划
	 *
	 * @param planId   计划ID
	 * @param title    计划标题
	 * @param steps    计划步骤列表
	 * @param context  工具执行环境
	 * @return 创建计划结果
	 */
	public String createPlan(String planId, String title, List<String> steps, ToolContext context) {
		OverAllState state = (OverAllState) context.getContext().get("state");

		if (planId == null || planId.isEmpty()) {
			throw new RuntimeException("Parameter `plan_id` is required for command: create");
		}

		if (plans.containsKey(planId)) {
			throw new RuntimeException(
					"A plan with ID '" + planId + "' already exists. Use 'update' to modify existing plans.");
		}

		if (title == null || title.isEmpty()) {
			throw new RuntimeException("Parameter `title` is required for command: create");
		}

		if (steps == null || steps.isEmpty() || !steps.stream().allMatch(step -> step instanceof String)) {
			throw new RuntimeException("Parameter `steps` must be a non-empty list of strings for command: create");
		}

		Map<String, Object> plan = new HashMap<>();
		plan.put("plan_id", planId);
		plan.put("title", title);
		plan.put("steps", steps);
		plan.put("step_statuses", new ArrayList<>(Collections.nCopies(steps.size(), PlanStateEnum.NOT_STARTED.getKey())));
		plan.put("step_notes", new ArrayList<>(Collections.nCopies(steps.size(), "")));

		plans.put(planId, plan);
		this.currentPlanId = planId; // Set as active plan

		List<Message> messages = (List<Message>) state.value("messages").get();
		Plan plan2 = new Plan(messages.get(0).getText(), planId, steps);
		graphPlans.put(planId, plan2);

		return planId;
	}

	/**
	 * 更新计划
	 *
	 * @param planId   计划ID
	 * @param title    计划标题
	 * @param steps    计划步骤列表
	 * @return 更新计划结果
	 */
	public PlanToolExecuteResult updatePlan(String planId, String title, List<String> steps) {
		if (planId == null || planId.isEmpty()) {
			throw new RuntimeException("Parameter `plan_id` is required for command: update");
		}

		if (!plans.containsKey(planId)) {
			throw new RuntimeException("No plan found with ID: " + planId);
		}

		Map<String, Object> plan = plans.get(planId);

		if (title != null && !title.isEmpty()) {
			plan.put("title", title);
		}

		if (steps != null) {
			if (!steps.stream().allMatch(step -> step instanceof String)) {
				throw new RuntimeException("Parameter `steps` must be a list of strings for command: update");
			}

			List<String> oldSteps = (List<String>) plan.get("steps");
			List<String> oldStatuses = (List<String>) plan.get("step_statuses");
			List<String> oldNotes = (List<String>) plan.get("step_notes");

			List<String> newStatuses = new ArrayList<>();
			List<String> newNotes = new ArrayList<>();


			// Loop through each step in the steps list
			// 遍历步骤列表中的每一个步骤
			for (int i = 0; i < steps.size(); i++) {
				String step = steps.get(i);

				// Check if current step exists in old steps and hasn't changed
				// 检查当前步骤是否存在于旧步骤中且未被修改
				if (i < oldSteps.size() && step.equals(oldSteps.get(i))) {
					// If step is unchanged, preserve its old status and notes
					// 如果步骤未改变，保留其旧状态和备注
					newStatuses.add(oldStatuses.get(i));
					newNotes.add(oldNotes.get(i));
				} else {
					// For new or modified steps, set default status and empty notes
					// 对于新的或被修改的步骤，设置默认状态和空备注
					newStatuses.add(PlanStateEnum.NOT_STARTED.getKey());
					newNotes.add("");
				}
			}

			plan.put("steps", steps);
			plan.put("step_statuses", newStatuses);
			plan.put("step_notes", newNotes);
		}

		return new PlanToolExecuteResult("Plan updated successfully: " + planId + "\n\n" + formatPlan(plan), planId);
	}

	/**
	 * 获取计划中步骤执行状态
	 *
	 * @return 获取计划结果
	 */
	public PlanToolExecuteResult listPlans() {
		if (plans.isEmpty()) {
			return new PlanToolExecuteResult("No plans available. Create a plan with the 'create' command.", "");
		}

		StringBuilder output = new StringBuilder("Available plans:\n");
		for (String planId : plans.keySet()) {
			Map<String, Object> plan = plans.get(planId);
			String currentMarker = planId.equals(currentPlanId) ? " (active)" : "";
			long completed = ((List<String>) plan.get("step_statuses")).stream()
					.filter(status -> PlanStateEnum.COMPLETED.getKey().equals(status))
					.count();
			int total = ((List<String>) plan.get("steps")).size();
			String progress = completed + "/" + total + " steps completed";
			output.append("• ")
					.append(planId)
					.append(currentMarker)
					.append(": ")
					.append(plan.get("title"))
					.append(" - ")
					.append(progress)
					.append("\n");
		}

		return new PlanToolExecuteResult(output.toString(), "");
	}

	/**
	 * 获取工作流图计划
	 *
	 * @param planId   计划ID
	 * @return 获取计划结果
	 */
	public Plan getGraphPlan(String planId) {
		if (StringUtils.isBlank(planId)) {
			if (currentPlanId == null) {
				throw new RuntimeException("No active plan. Please specify a plan_id or set an active plan.");
			}
			planId = currentPlanId;
		}

		if (!plans.containsKey(planId)) {
			throw new RuntimeException("No plan found with ID: " + planId);
		}

		return graphPlans.get(planId);
	}

	/**
	 * 获取计划执行结果
	 *
	 * @param planId   计划ID
	 * @return 获取计划结果
	 */
	public PlanToolExecuteResult getPlan(String planId) {
		if (StringUtils.isBlank(planId)) {
			if (currentPlanId == null) {
				throw new RuntimeException("No active plan. Please specify a plan_id or set an active plan.");
			}
			planId = currentPlanId;
		}

		if (!plans.containsKey(planId)) {
			throw new RuntimeException("No plan found with ID: " + planId);
		}

		Map<String, Object> plan = plans.get(planId);
		return new PlanToolExecuteResult(formatPlan(plan), planId);
	}

	/**
	 * 计划激活
	 *
	 * @param planId   计划ID
	 * @return 获取计划结果
	 */
	public PlanToolExecuteResult setActivePlan(String planId) {
		if (StringUtils.isBlank(planId)) {
			throw new RuntimeException("Parameter `plan_id` is required for command: set_active");
		}

		if (!plans.containsKey(planId)) {
			throw new RuntimeException("No plan found with ID: " + planId);
		}

		currentPlanId = planId;
		return new PlanToolExecuteResult(
				"Plan '" + planId + "' is now the active plan.\n\n" + formatPlan(plans.get(planId)), planId);
	}

	/**
	 * 标记计划的步骤状态和说明
	 * @param planId   计划ID
	 * @param stepIndex 步骤索引
	 * @param stepStatus 步骤状态 "not_started", "active", "completed", "failed"
	 * @param stepNotes 步骤说明
	 *
	 * @return 获取计划结果
	 */
	public PlanToolExecuteResult markStep(String planId, Integer stepIndex, String stepStatus, String stepNotes) {
		if (planId == null || planId.isEmpty()) {
			if (currentPlanId == null) {
				throw new RuntimeException("No active plan. Please specify a plan_id or set an active plan.");
			}
			planId = currentPlanId;
		}

		if (!plans.containsKey(planId)) {
			throw new RuntimeException("No plan found with ID: " + planId);
		}

		if (stepIndex == null) {
			throw new RuntimeException("Parameter `step_index` is required for command: mark_step");
		}

		Map<String, Object> plan = plans.get(planId);
		List<String> steps = (List<String>) plan.get("steps");

		if (stepIndex < 0 || stepIndex >= steps.size()) {
			throw new RuntimeException(
					"Invalid step_index: " + stepIndex + ". Valid indices range from 0 to " + (steps.size() - 1) + ".");
		}

		List<String> stepStatuses = (List<String>) plan.get("step_statuses");
		List<String> stepNotesList = (List<String>) plan.get("step_notes");

		if (stepStatus != null
				&& !Arrays.asList("not_started", "in_progress", "completed", "blocked").contains(stepStatus)) {
			throw new RuntimeException("Invalid step_status: " + stepStatus
					+ ". Valid statuses are: not_started, in_progress, completed, blocked");
		}

		if (stepStatus != null) {
			stepStatuses.set(stepIndex, stepStatus);
		}

		if (stepNotes != null) {
			stepNotesList.set(stepIndex, stepNotes);
		}

		String result = "Step " + stepIndex + " updated in plan '" + planId + "'.\n\n" + formatPlan(plan);
		log.info(result);
		return new PlanToolExecuteResult(result, planId);
	}

	/**
	 * 删除计划
	 * @param planId   计划ID
	 * @return 获取计划结果
	 *
	 */
	public PlanToolExecuteResult deletePlan(String planId) {
		if (planId == null || planId.isEmpty()) {
			throw new RuntimeException("Parameter `plan_id` is required for command: delete");
		}

		if (!plans.containsKey(planId)) {
			throw new RuntimeException("No plan found with ID: " + planId);
		}

		plans.remove(planId);

		if (planId.equals(currentPlanId)) {
			currentPlanId = null;
		}

		return new PlanToolExecuteResult("Plan '" + planId + "' has been deleted.", planId);
	}

	private String formatPlan(Map<String, Object> plan) {
		StringBuilder output = new StringBuilder();
		String planTitle = (String) plan.get("title");
		String planId = (String) plan.get("plan_id");

		output.append("Plan: ").append(planTitle).append(" (ID: ").append(planId).append(")\n");
		output.append(repeatString("=", output.length())).append("\n\n");

		// Calculate progress statistics
		List<String> steps = (List<String>) plan.get("steps");
		List<String> stepStatuses = (List<String>) plan.get("step_statuses");
		List<String> stepNotes = (List<String>) plan.get("step_notes");

		int totalSteps = steps.size();
		long completed = stepStatuses.stream().filter(status -> "completed".equals(status)).count();
		long inProgress = stepStatuses.stream().filter(status -> "in_progress".equals(status)).count();
		long blocked = stepStatuses.stream().filter(status -> "blocked".equals(status)).count();
		long notStarted = stepStatuses.stream().filter(status -> "not_started".equals(status)).count();

		output.append("Progress: ").append(completed).append("/").append(totalSteps).append(" steps completed ");
		if (totalSteps > 0) {
			double percentage = (completed / (double) totalSteps) * 100;
			output.append(String.format("(%.1f%%)\n", percentage));
		}
		else {
			output.append("(0%)\n");
		}

		output.append("Status: ")
				.append(completed)
				.append(" completed, ")
				.append(inProgress)
				.append(" in progress, ")
				.append(blocked)
				.append(" blocked, ")
				.append(notStarted)
				.append(" not started\n\n");
		output.append("Steps:\n");

		// Add each step with its status and notes
		for (int i = 0; i < totalSteps; i++) {
			String step = steps.get(i);
			String status = stepStatuses.get(i);
			String notes = stepNotes.get(i);

			String statusSymbol;
			switch (status) {
				case "in_progress":
					statusSymbol = "[→]";
					break;
				case "completed":
					statusSymbol = "[✓]";
					break;
				case "blocked":
					statusSymbol = "[!]";
					break;
				default:
					statusSymbol = "[ ]";
			}

			output.append(i).append(". ").append(statusSymbol).append(" ").append(step).append("\n");
			if (notes != null && !notes.isEmpty()) {
				output.append("   Notes: ").append(notes).append("\n");
			}
		}

		return output.toString();
	}

	private String repeatString(String str, int times) {
		StringBuilder repeated = new StringBuilder();
		for (int i = 0; i < times; i++) {
			repeated.append(str);
		}
		return repeated.toString();
	}

	public Map<String, Map<String, Object>> getPlans() {
		return plans;
	}

	public void setPlans(Map<String, Map<String, Object>> plans) {
		this.plans = plans;
	}

	public String getCurrentPlanId() {
		return currentPlanId;
	}

	public void setCurrentPlanId(String currentPlanId) {
		this.currentPlanId = currentPlanId;
	}


	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonClassDescription("Planning Tool request")
	public static record Request(
			@JsonProperty(required = true,value = "command")
			@JsonPropertyDescription("The command to execute. Available commands: create, update, list, get, set_active, mark_step, delete. enum: [\"create\"]")
			String command,
			@JsonProperty(required = true,value = "plan_id")
			@JsonPropertyDescription("Unique identifier for the plan. Required for create, update, set_active, and delete commands. Optional for get and mark_step (uses active plan if not specified).")
			String plan_id,
			@JsonProperty(required = false,value = "title")
			@JsonPropertyDescription("Title for the plan. Required for create command, optional for update command.")
			String title,
			@JsonProperty(required = false,value = "steps")
			@JsonPropertyDescription("List of plan steps. Required for create command, optional for update command.")
			List<String> steps,
			@JsonProperty(required = false,value = "step_index")
			@JsonPropertyDescription("The number of days for which the weather is forecasted")
			int step_index,
			@JsonProperty(required = false,value = "step_status")
			@JsonPropertyDescription("Status to set for a step. Used with mark_step command. enum: [\"not_started\", \"in_progress\", \"completed\", \"blocked\"]")
			String step_status,
			@JsonProperty(required = false,value = "step_notes")
			@JsonPropertyDescription("Additional notes for a step. Optional for mark_step command.")
			String step_notes
	) {}

    @Override
    public String apply(Request request, ToolContext context) {
		return run(request, context);
    }
}
