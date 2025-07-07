package com.ivan.researchagent.springai.agent.graph.doctor.node;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.async.AsyncGenerator;
import com.alibaba.cloud.ai.graph.streaming.StreamingChatGenerator;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.ivan.researchagent.common.utils.StringTemplateUtil;
import com.ivan.researchagent.springai.agent.constant.PromptConstant;
import com.ivan.researchagent.springai.agent.tool.DoctorTools;
import com.ivan.researchagent.springai.llm.model.chat.ChatRequest;
import com.ivan.researchagent.springai.llm.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.Arrays;
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
public class DoctorUpdateNode implements NodeAction {

    private ChatService chatService;
    private DoctorTools doctorTools;

    public DoctorUpdateNode(ChatService chatService, DoctorTools doctorTools) {
        this.chatService = chatService;
        this.doctorTools = doctorTools;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("doctor update node is running.");

        String feedBack = state.value("feed_back", "");
        String queryResult = state.value("query_result", "");

        if (StringUtils.isBlank(queryResult)) {
            return Map.of("chat_result", "没有可更新数据");
        }

        String request = state.value("chat_request", "");
        if (StringUtils.isBlank(request)) {
            return Map.of("chat_result", "请输入操作指令");
        }
        ChatRequest chatRequest = JSON.parseObject(request, ChatRequest.class);
        chatRequest.setDefaultSystem(StringTemplateUtil.render(PromptConstant.DOCTOR_UPDATE_PROMPT, Map.of("data", queryResult)));
        chatRequest.setMessages(Lists.newArrayList());
        chatRequest.setUserMessage(feedBack);
        chatRequest.setTools(Arrays.asList(doctorTools));

        Flux<ChatResponse> chatResponseFlux = chatService.steamChat(chatRequest);

        AsyncGenerator<? extends NodeOutput> generator = StreamingChatGenerator.builder()
                .startingNode("llm_stream")
                .startingState(state)
                .mapResult(response -> {
                    String text = response.getResult().getOutput().getText();
                    List<String> queryVariants = Arrays.asList(text.split("\n"));
                    return Map.of("chat_request", JSON.toJSONString(chatRequest), "chat_result", queryVariants);
                }).build(chatResponseFlux);

        return Map.of("chat_result", generator);
    }
}
