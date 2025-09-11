package com.ivan.researchagent.springai.agent.graph.agent.doctor;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.async.AsyncGenerator;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.state.StateSnapshot;
import com.alibaba.fastjson.JSON;
import com.ivan.researchagent.springai.agent.graph.core.GraphProcess;
import com.ivan.researchagent.springai.llm.model.chat.ChatParams;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
@Service
public class DoctorGraphAgent {

    @Resource(name = "doctorCompiledGraph")
    private CompiledGraph compiledGraph;

    public Flux<ServerSentEvent<String>> sseChat(ChatParams chatParams) throws GraphRunnerException {
        boolean isNewSession = false;
        //对话会话的唯一标识
        String sessionId = chatParams.getConversantId();
        if (StringUtils.isBlank(sessionId)) {
            isNewSession = true;
            sessionId = UUID.randomUUID().toString();
            chatParams.setConversantId(sessionId);
            log.info("sessionId:{}", sessionId);
        }

        // 将chatRequest转换成Map
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("chat_request", JSON.toJSONString(chatParams));

        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(sessionId).build();

        // Create a unicast sink to emit ServerSentEvents
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().unicast().onBackpressureBuffer();
        GraphProcess graphProcess = new GraphProcess(this.compiledGraph);
        AsyncGenerator<NodeOutput> resultFuture;

        if (isNewSession) {
            objectMap.put("query", chatParams.findUserMessage());
            resultFuture = compiledGraph.stream(objectMap, runnableConfig);
        } else {
            StateSnapshot stateSnapshot = this.compiledGraph.getState(runnableConfig);
            OverAllState state = stateSnapshot.state();
            state.withResume();

            objectMap.put("feed_back", chatParams.findUserMessage());
            state.withHumanFeedback(new OverAllState.HumanFeedback(objectMap, ""));
            resultFuture = compiledGraph.streamFromInitialNode(state, runnableConfig);
        }

        graphProcess.processStream(resultFuture, sink);
        return sink.asFlux()
                .doOnCancel(() -> log.info("Client disconnected from stream"))
                .doOnError(e -> log.error("Error occurred during streaming", e));
    }
}
