package com.ivan.researchagent.main.controller;

import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplate;
import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplateFactory;
import com.ivan.researchagent.main.model.prompt.PromptTemplateQuery;
import com.ivan.researchagent.springai.llm.service.ChatService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/11/29 15:28
 **/
//@RestController
//@RequestMapping("/prompt")
//@CrossOrigin(origins = "*")
public class PromptController {

    @jakarta.annotation.Resource
    private ChatService chatService;
    @jakarta.annotation.Resource
    private ConfigurablePromptTemplateFactory configurablePromptTemplateFactory;

    @Value("classpath:/doc/wikipedia-curling.md")
    private Resource docToStuffResource;

    @Value("classpath:/prompt/template/system-message.st")
    private Resource systemTemplateResource;

    @Value("classpath:/prompt/template/context-prompt.st")
    private Resource contextPromptResource;


    @GetMapping("/role-prompt")
    public AssistantMessage rolePrompt(@RequestParam(value = "userInput", defaultValue = "Tell me about ...") String userInput,
                                           @RequestParam(value = "systemInput", defaultValue = "You are a helpful AI assistant.") String systemInput) {
        List<Message> messages = new ArrayList<>();
        UserMessage userMessage = new UserMessage(userInput);
        messages.add(userMessage);
        if (StringUtils.isNotBlank(systemInput)) {
            SystemMessage systemMessage = new SystemMessage(systemInput);
            messages.add(systemMessage);
        }

        Prompt prompt = new Prompt(messages);

        ChatResponse response = chatService.chat(prompt);
        return response.getResult().getOutput();
    }

    @GetMapping("/system-template")
    public AssistantMessage systemTemplate(@RequestParam(value = "message", defaultValue = "Tell me about ...") String message,
                                     @RequestParam(value = "name", defaultValue = "Bob") String name,
                                     @RequestParam(value = "voice", defaultValue = "pirate") String voice) {
        UserMessage userMessage = new UserMessage(message);
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemTemplateResource);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("name", name, "voice", voice));
        Prompt prompt = new Prompt(List.of(userMessage, systemMessage));

        ChatResponse response = chatService.chat(prompt);
        return response.getResult().getOutput();
    }

    @GetMapping("/prompt-template")
    public AssistantMessage generate(@RequestBody PromptTemplateQuery templateQuery) {
        ConfigurablePromptTemplate template = configurablePromptTemplateFactory.getTemplate(templateQuery.getTemplateName());
        if (template == null) {
            template = configurablePromptTemplateFactory.create(templateQuery.getTemplateName(),
                    templateQuery.getTemplateContent());
        }
        Prompt prompt;
        if (MapUtils.isNotEmpty(templateQuery.getTemplateParam())) {
            prompt = template.create(templateQuery.getTemplateParam());
        } else {
            prompt = template.create();
        }

        ChatResponse response = chatService.chat(prompt);
        return response.getResult().getOutput();
    }

    @GetMapping("/context-template")
    public AssistantMessage completion(@RequestParam(value = "message", defaultValue = "") String message,
                                 @RequestParam(value = "addContext", defaultValue = "false") boolean addContext) {
        PromptTemplate promptTemplate = new PromptTemplate(contextPromptResource);
        Map<String, Object> map = new HashMap<>();
        map.put("question", message);
        if (addContext) {
            map.put("context", docToStuffResource);
        }
        else {
            map.put("context", "");
        }
        Prompt prompt = promptTemplate.create(map);

        ChatResponse response = chatService.chat(prompt);
        return response.getResult().getOutput();
    }
}
