package com.ivan.researchagent.main.controller;

import com.ivan.researchagent.springai.llm.model.chat.ChatRequest;
import com.ivan.researchagent.springai.llm.model.chat.ChatResult;
import com.ivan.researchagent.springai.agent.agentic.biz.DoctorOperateAgent;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/11/28 13:43
 **/
@Slf4j
@RestController
@RequestMapping("/agent/doctor")
@CrossOrigin(origins = "*") // 支持所有来源的跨域请求
public class DoctorAgentController {

    @Resource
    private DoctorOperateAgent doctorOperateAgent;


    @GetMapping("")
    public String chat(@RequestBody ChatRequest chatRequest, HttpServletRequest request, HttpServletResponse response) {

        String sessionId = chatRequest.getSessionId();
        if (StringUtils.isBlank(sessionId)) {
            sessionId = request.getHeader("sessionId");
            chatRequest.setSessionId(sessionId);
        }

        ChatResult chatResult = doctorOperateAgent.call(chatRequest);

        response.setHeader("sessionId", chatResult.getSessionId());
        return chatResult.getContent();
    }

    @GetMapping("/stream")
    public Flux<String> streamChat(@RequestBody ChatRequest chatRequest, HttpServletRequest request, HttpServletResponse response) {

        String sessionId = chatRequest.getSessionId();
        if (StringUtils.isBlank(sessionId)) {
            sessionId = request.getHeader("sessionId");
            chatRequest.setSessionId(sessionId);
        }

        Flux<ChatResult> chatResult = doctorOperateAgent.stream(chatRequest);

        return chatResult.map(result -> {
            log.info("sessionId:{}, streamChat result:{}", result.getSessionId(), result.getContent());
            response.setHeader("sessionId", result.getSessionId());
            return result.getContent();
        });
    }

}
