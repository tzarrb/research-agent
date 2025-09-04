package com.ivan.researchagent.springai.agent.graph.agent.customer.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/9/1/周一
 **/
@Slf4j
public class RecordingNode implements NodeAction {
    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String feedBack = state.value("input", "");
        String classifier = state.value("classifier_output", "");
        log.info("反馈内容: {}, 分类结果: {}", feedBack, classifier);
        if (classifier.toLowerCase().contains("positive")) {
            return Map.of("response", "感谢你的反馈与支持，我们将继续努力，为客户提供更好的服务");
        } else if (classifier.toLowerCase().contains("after-sale service")) {
            return Map.of("response", "您反馈的售后服务问题我们将积极改进，感谢你的反馈与支持，我们将继续努力，为客户提供更好的服务");
        } else if (classifier.toLowerCase().contains("transportation")) {
            return Map.of("response", "您反馈的快递运输问题我们将积极改进，感谢你的反馈与支持，我们将继续努力，为客户提供更好的服务");
        } else if (classifier.toLowerCase().contains("product quality")) {
            return Map.of("response", "您反馈的商品质量问题我们将积极改进，感谢你的反馈与支持，我们将继续努力，为客户提供更好的服务");
        } else {
            return Map.of("response", "您反馈的问题我们将积极改进，感谢你的反馈与支持，我们将继续努力，为客户提供更好的服务");
        }
    }
}
