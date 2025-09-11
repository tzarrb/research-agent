package com.ivan.researchagent.springai.llm.model.chat;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.Lists;
import com.ivan.researchagent.core.model.ChatRoleMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/03 15:20
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatParams implements Serializable {

    @JsonPropertyDescription("大模型提供商")
    private String provider;

    @JsonPropertyDescription("大模型名称")
    private String model;

    @JsonPropertyDescription("是否使用对话记忆")
    private Boolean enableMemory = true;

    @JsonPropertyDescription("是否流式对话方法")
    private Boolean enableStream = true;

    @JsonPropertyDescription("是否支持多模态")
    private Boolean enableMulti = false;

    @JsonPropertyDescription("是否使用智能体")
    private Boolean enableAgent = false;

    @JsonPropertyDescription("是否使用本地知识库")
    private Boolean enableLocal = false;

    @JsonPropertyDescription("是否联网搜索")
    private Boolean enableWeb = false;

    @JsonPropertyDescription("是否深度思考")
    private Boolean enableThink = false;

    @JsonPropertyDescription("对话会话ID，也是对话记忆的唯一标识")
    private String conversantId;

    @JsonPropertyDescription("聊天对话调用工具")
    private List<Object> tools;

    @JsonPropertyDescription("聊天对话调用工具名称")
    private List<String> toolNames;

    @JsonPropertyDescription("聊天对话调用工具回调")
    private List<ToolCallback> toolCallBacks;

    @JsonPropertyDescription("聊天对话调用工具回调提供者")
    private List<ToolCallbackProvider> toolCallbackProviders;

    @JsonPropertyDescription("大模型默认工具")
    private List<Object> defaultTools;

    @JsonPropertyDescription("大模型默认工具名称")
    private List<String> defaultToolNames;

    @JsonPropertyDescription("大模型默认工具回调")
    private List<ToolCallback> defaultToolCallbacks;

    @JsonPropertyDescription("大模型默认工具回调提供者")
    private List<ToolCallbackProvider> defaultToolCallbackProviders;

    @JsonPropertyDescription("输出格式, 如：bean, list, map, json")
    private String formatType;

    @JsonPropertyDescription("智能体")
    private String agent;

    @JsonPropertyDescription("对话信息媒体类型，如：TEXT,IMAGE,VIDEO,AUDIO")
    private String messageType = "TEXT";

    @JsonPropertyDescription("系统提示词")
    private String defaultSystem;

    @JsonPropertyDescription("对话输入信息,比如 [{\"role\":\"user\",\"content\":\"我想...\"}]")
    private List<ChatRoleMessage> messages;

    /**
     * 设置系统角色提示词
     *
     * @param systemPrompt
     */
    public void addSystemMessage(String systemPrompt) {
        if (StringUtils.isBlank(systemPrompt)) {
            return;
        }

        if (Objects.isNull(this.messages)) {
            this.messages = Lists.newArrayList(ChatRoleMessage.builder().role(MessageType.SYSTEM.getValue()).content(systemPrompt).build());
        } else {
            this.messages = this.messages.stream()
                    .filter(message -> !MessageType.SYSTEM.getValue().equals(message.getRole()))
                    .collect(Collectors.toList());
            this.messages.add(0, ChatRoleMessage.builder().role(MessageType.SYSTEM.getValue()).content(systemPrompt).build());
        }
    }

    public String findSystemMessage() {
        if (Objects.isNull(this.messages)) {
            return "";
        }

        return this.messages.stream()
                .filter(message -> MessageType.SYSTEM.getValue().equals(message.getRole()))
                .findFirst()
                .map(ChatRoleMessage::getContent)
                .orElse("");
    }

    public void addUserMessage(String userInput) {
        addUserMessage(userInput, null, null);
    }

    public void addUserMessage(String userInput, List<String> mediaUrls, MultipartFile mediaFile) {
        if (StringUtils.isBlank(userInput)) {
            return;
        }

        ChatRoleMessage userMessage = ChatRoleMessage.builder()
                .role(MessageType.USER.getValue())
                .content(userInput)
                .mediaUrls(mediaUrls)
                .mediaFile(mediaFile)
                .build();
        if (Objects.isNull(this.messages)) {
            this.messages = Lists.newArrayList(userMessage);
        } else {
//            ChatRoleMessage userMessage = this.messages.stream()
//                    .filter(message -> MessageType.USER.getValue().equals(message.getRole()))
//                    .findFirst()
//                    .orElse(ChatRoleMessage.builder().role(MessageType.USER.getValue()).build());
//            userMessage.setContent(userInput);
//            this.messages.remove(userMessage);
//            this.messages.add(userMessage);
            this.messages = this.messages.stream()
                    .filter(message -> !MessageType.USER.getValue().equals(message.getRole()))
                    .collect(Collectors.toList());
            this.messages.add(userMessage);
        }
    }

    public String findUserMessage() {
        if (Objects.isNull(this.messages)) {
            return "";
        }

        return this.messages.stream()
                .filter(message -> MessageType.USER.getValue().equals(message.getRole()))
                .findFirst()
                .map(ChatRoleMessage::getContent)
                .orElse("");
    }
}
