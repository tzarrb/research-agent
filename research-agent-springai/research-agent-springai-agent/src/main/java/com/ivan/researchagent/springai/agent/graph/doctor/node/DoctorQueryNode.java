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
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
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
public class DoctorQueryNode implements NodeAction {

    private ChatService chatService;
    private DoctorTools doctorTools;

    public DoctorQueryNode(ChatService chatService, DoctorTools doctorTools) {
        this.chatService = chatService;
        this.doctorTools = doctorTools;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("doctor query node is running.");

        String request = state.value("chat_request", "");
        if (StringUtils.isBlank(request)) {
            return Map.of("chat_result", "请输入查询指令");
        }

        ChatRequest chatRequest = JSON.parseObject(request, ChatRequest.class);
        chatRequest.setDefaultSystem(PromptConstant.DOCTOR_QUERY_PROMPT);
        String feedBack = state.value("feed_back", chatRequest.getUserMessage());
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
                    return Map.of("query", feedBack , "chat_request", JSON.toJSONString(chatRequest), "query_result", text, "chat_result", text);
                }).build(chatResponseFlux);

        return Map.of("chat_result", generator);
    }
}
