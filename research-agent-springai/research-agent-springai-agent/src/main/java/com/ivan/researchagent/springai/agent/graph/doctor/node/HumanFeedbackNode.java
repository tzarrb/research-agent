package com.ivan.researchagent.springai.agent.graph.doctor.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.google.common.collect.Lists;
import com.ivan.researchagent.springai.llm.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

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
 * @since: 2025/7/3/周四
 **/
@Slf4j
public class HumanFeedbackNode implements NodeAction {

    private ChatService chatService;

    private static final List<String> QUERY_FEEDBACKS = Lists.newArrayList("重新", "查询", "搜索", "查找");
    private static final List<String> OPERATE_FEEDBACKS = Lists.newArrayList("注销", "删除", "重置");
    private static final List<String> UPDATE_FEEDBACKS = Lists.newArrayList("修改", "更新");

    public HumanFeedbackNode(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("human_feedback node is running.");
        HashMap<String, Object> resultMap = new HashMap<>();
        String nextStep = StateGraph.END;

        Map<String, Object> feedBackData = state.humanFeedback().data();
        String feedback = (String) feedBackData.getOrDefault("feed_back", "");
        if (StringUtils.isNotBlank(feedback)) {
            // 下一步查询节点
            if (QUERY_FEEDBACKS.stream().anyMatch(feedback::contains)) {
                nextStep = DoctorQueryNode.class.getSimpleName();
                resultMap.put("feed_back", feedback);
            }
            // 下一步操作节点
            if (OPERATE_FEEDBACKS.stream().anyMatch(feedback::contains)) {
                nextStep = DoctorOperateNode.class.getSimpleName();
            }
            // 下一步更新节点
            if (UPDATE_FEEDBACKS.stream().anyMatch(feedback::contains)) {
                nextStep = DoctorUpdateNode.class.getSimpleName();
            }

        }

        resultMap.put("feed_back", feedback);
        resultMap.put("human_next_node", nextStep);
        log.info("human_feedback node -> {} node, feedback: {}", nextStep, feedback);
        return resultMap;
    }
}
