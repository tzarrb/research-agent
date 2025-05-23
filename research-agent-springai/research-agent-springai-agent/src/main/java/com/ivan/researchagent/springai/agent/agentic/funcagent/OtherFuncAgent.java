package com.ivan.researchagent.springai.agent.agentic.funcagent;

import com.alibaba.fastjson.JSON;
import com.ivan.researchagent.common.constant.Constant;
import com.ivan.researchagent.springai.agent.anno.FuncAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.context.annotation.Description;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @author: ivan
 * @since: 2025/1/4 23:42
 */
@Slf4j
@FuncAgent
@Description("未明确分类的任务的智能体")
public class OtherFuncAgent extends AbstractFuncAgent<LinkedHashMap<String, String>, ToolContext, String> {

    @Override
    List<String> getFunctions() {
        return null;
    }

    @Override
    public String apply(LinkedHashMap<String, String> request, ToolContext toolContext) {
        String input = request.values().stream().findFirst().get();
        ChatClient.ChatClientRequestSpec requestSpec = buildRequestSpec(input, "", toolContext);
        ChatResponse response = requestSpec.call().chatResponse();
        String content = response.getResult().getOutput().getText();

        String sessionId = (String) toolContext.getContext().get(Constant.CONVERSANT_ID);
        log.info("sessionId:{}, agent request:{}, response: {}", sessionId, JSON.toJSONString(request), content);
        return content;
    }
}
