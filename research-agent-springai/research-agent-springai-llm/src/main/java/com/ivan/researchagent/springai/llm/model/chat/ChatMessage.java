package com.ivan.researchagent.springai.llm.model.chat;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.Lists;
import com.ivan.researchagent.common.model.ChatRoleMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;

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
public class ChatMessage implements Serializable {

    @JsonPropertyDescription("大模型提供商")
    private String provider = "dashscope";

    @JsonPropertyDescription("大模型名称")
    private String model = "qwen-max";

    @JsonPropertyDescription("是否使用对话记忆")
    private Boolean enableMemory = true;

    @JsonPropertyDescription("是否流式对话方法")
    private Boolean enableStream = false;

    @JsonPropertyDescription("是否使用智能体")
    private Boolean enableAgent = false;

    @JsonPropertyDescription("是否使用本地知识库")
    private Boolean enableLocal = false;

    @JsonPropertyDescription("是否联网搜索")
    private Boolean enableWeb = false;

    @JsonPropertyDescription("对话会话ID，也是对话记忆的唯一标识")
    private String sessionId;

    @JsonPropertyDescription("大模型调用工具")
    private List<Object> tools;

    @JsonPropertyDescription("大模型调用工具名称")
    private List<String> toolNames;

    @JsonPropertyDescription("大模型调用工具回调")
    private List<ToolCallback> toolCallBacks;

    @JsonPropertyDescription("大模型调用工具回调提供者")
    private List<ToolCallbackProvider> toolCallbackProviders;

    @JsonPropertyDescription("输出格式, 如：bean, list, map, json")
    private String formatType;

    @JsonPropertyDescription("智能体")
    private String agent;

    @JsonPropertyDescription("对话输入信息类型，如：TEXT,IMAGE,VIDEO")
    private String messageType = "TEXT";

    @JsonPropertyDescription("对话输入信息,比如 [{\"role\":\"user\",\"content\":\"我想...\"}]")
    private List<ChatRoleMessage> messages;

    /**
     * 设置系统角色提示词
     *
     * @param systemPrompt
     */
    public void setSystemMessage(String systemPrompt) {
        if (StringUtils.isBlank(systemPrompt)) {
            return;
        }

        if (Objects.isNull(this.messages)) {
            this.messages = Lists.newArrayList(ChatRoleMessage.builder().role(MessageType.SYSTEM.getValue()).content(systemPrompt).build());
        } else {
            this.messages = this.messages.stream()
                    .filter(message -> !MessageType.SYSTEM.getValue().equals(message.getRole()))
                    .collect(Collectors.toList());
            this.messages.add(ChatRoleMessage.builder().role(MessageType.SYSTEM.getValue()).content(systemPrompt).build());
        }
    }

    public String getSystemMessage() {
        if (Objects.isNull(this.messages)) {
            return "";
        }

        return this.messages.stream()
                .filter(message -> MessageType.SYSTEM.getValue().equals(message.getRole()))
                .findFirst()
                .map(ChatRoleMessage::getContent)
                .orElse("");
    }

    public void setUserMessage(String userInput) {
        if (StringUtils.isBlank(userInput)) {
            return;
        }

        if (Objects.isNull(this.messages)) {
            this.messages = Lists.newArrayList(ChatRoleMessage.builder().role(MessageType.USER.getValue()).content(userInput).build());
        } else {
            ChatRoleMessage userMessage = this.messages.stream()
                    .filter(message -> MessageType.USER.getValue().equals(message.getRole()))
                    .findFirst()
                    .orElse(ChatRoleMessage.builder().role(MessageType.USER.getValue()).build());
            userMessage.setContent(userInput);
            this.messages.remove(userMessage);
            this.messages.add(userMessage);
        }
    }

    public String getUserMessage() {
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
