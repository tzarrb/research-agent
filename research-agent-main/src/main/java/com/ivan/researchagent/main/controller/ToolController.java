package com.ivan.researchagent.main.controller;

import com.google.common.collect.Lists;
import com.ivan.researchagent.springai.llm.service.ChatService;
import com.ivan.researchagent.springai.agent.model.bo.doctor.DoctorInfoBO;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/02 17:22
 **/
@RestController
@RequestMapping("/tool")
@CrossOrigin(origins = "*")
public class ToolController {

    @jakarta.annotation.Resource
    private ChatService chatService;

    @GetMapping("/provider-by-mobile")
    public DoctorInfoBO getProviderByMobile(String input) {
        DoctorInfoBO doctorInfoBO = chatService.toolChatObject(input, Lists.newArrayList("getProviderByMobileFunction"), DoctorInfoBO.class);
        return doctorInfoBO;
    }

    @GetMapping("/provider-by-name")
    public List<DoctorInfoBO> listProviderByName(String input) {
        return chatService.toolChatArray(input, Lists.newArrayList("listProviderByNameFunction"));
    }

}
