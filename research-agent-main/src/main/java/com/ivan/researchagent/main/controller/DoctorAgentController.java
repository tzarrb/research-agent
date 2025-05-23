package com.ivan.researchagent.main.controller;

import com.ivan.researchagent.springai.llm.model.ChatMessage;
import com.ivan.researchagent.springai.llm.model.ChatResult;
import com.ivan.researchagent.springai.agent.chat.DoctorOperateChat;
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
    private DoctorOperateChat doctorOperateAgent;


    @GetMapping("")
    public String chat(@RequestBody ChatMessage chatMessage, HttpServletRequest request, HttpServletResponse response) {

        String sessionId = chatMessage.getSessionId();
        if (StringUtils.isBlank(sessionId)) {
            sessionId = request.getHeader("sessionId");
            chatMessage.setSessionId(sessionId);
        }

        ChatResult chatResult = doctorOperateAgent.call(chatMessage);

        response.setHeader("sessionId", chatResult.getSessionId());
        return chatResult.getContent();
    }

    @GetMapping("/stream")
    public Flux<String> streamChat(@RequestBody ChatMessage chatMessage, HttpServletRequest request, HttpServletResponse response) {

        String sessionId = chatMessage.getSessionId();
        if (StringUtils.isBlank(sessionId)) {
            sessionId = request.getHeader("sessionId");
            chatMessage.setSessionId(sessionId);
        }

        Flux<ChatResult> chatResult = doctorOperateAgent.stream(chatMessage);

        return chatResult.map(result -> {
            log.info("sessionId:{}, streamChat result:{}", result.getSessionId(), result.getContent());
            response.setHeader("sessionId", result.getSessionId());
            return result.getContent();
        });
    }

}
