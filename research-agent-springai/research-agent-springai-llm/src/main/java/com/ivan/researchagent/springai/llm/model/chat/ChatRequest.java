package com.ivan.researchagent.springai.llm.model.chat;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.ivan.researchagent.common.utils.MediaUtil;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/5/14/周三
 **/
@Data
public class ChatRequest implements Serializable {

    @JsonPropertyDescription("大模型提供商")
    private String provider;

    @JsonPropertyDescription("大模型名称")
    private String model;

    @JsonPropertyDescription("用户输入")
    private String userMessage;

    @JsonPropertyDescription("系统消息")
    private String systemMessage;

    @JsonPropertyDescription("系统提示词")
    private String defaultSystem;

    @JsonPropertyDescription("对话记忆的唯一标识")
    private String conversantId;

    @JsonPropertyDescription("是否使用对话记忆")
    private Boolean enableMemory = true;

    @JsonPropertyDescription("是否流式对话方法")
    private Boolean enableStream = true;

    @JsonPropertyDescription("是否联网搜索")
    private Boolean enableWeb = false;

    @JsonPropertyDescription("是否支持本地知识库")
    private Boolean enableLocal = false;

    @JsonPropertyDescription("是否深度思考")
    private Boolean enableThink = false;

    @JsonPropertyDescription("对话的媒体链接")
    private List<String> mediaUrls;

    @JsonPropertyDescription("对话的媒体文件")
    private MultipartFile mediaFile;

    public ChatParams convertParams() {
        ChatParams chatParams = ChatParams.builder()
                .provider(provider)
                .model(model)
                .defaultSystem(defaultSystem)
                .conversantId(conversantId)
                .enableMemory(enableMemory)
                .enableStream(enableStream)
                .enableWeb(enableWeb)
                .enableLocal(enableLocal)
                .enableThink(enableThink)
                .enableAgent(false)
                .build();

        chatParams.addUserMessage(userMessage, mediaUrls, mediaFile);
        chatParams.addSystemMessage(systemMessage);

        if (CollectionUtils.isNotEmpty(mediaUrls) || Objects.nonNull(mediaFile)) {
            chatParams.setEnableMulti(true);
            if (CollectionUtils.isNotEmpty(mediaUrls)) {
                chatParams.setMessageType(MediaUtil.getContentType(mediaUrls.get(0)).name());
            }
            if (Objects.nonNull(mediaFile)) {
                chatParams.setMessageType(MediaUtil.getContentType(mediaFile).name());
            }
        }

        if (BooleanUtils.isTrue(enableThink) && StringUtils.isAllBlank(provider, model)) {
            chatParams.setProvider("dashscope");
        }
        return chatParams;
    }
}
