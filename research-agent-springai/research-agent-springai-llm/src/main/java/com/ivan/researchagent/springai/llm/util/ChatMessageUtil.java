package com.ivan.researchagent.springai.llm.util;

import com.alibaba.cloud.ai.dashscope.common.DashScopeApiConstants;
import com.alibaba.fastjson.JSON;
import com.ivan.researchagent.common.enumerate.MessageTypeEnum;
import com.ivan.researchagent.common.utils.MediaUtil;
import com.ivan.researchagent.springai.llm.model.chat.ChatParams;
import com.ivan.researchagent.core.model.ChatRoleMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.content.Media;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @author: ivan
 * @since: 2025/1/4 18:02
 */
@Slf4j
public class ChatMessageUtil {

    public static List<Message> buildMessages(ChatParams chatParams) {
        List<Message> messages = new ArrayList<>();
        for (ChatRoleMessage roleMessage : chatParams.getMessages()) {
            if (StringUtils.isBlank(roleMessage.getContent())
                    && CollectionUtils.isEmpty(roleMessage.getMediaUrls()) && Objects.isNull(roleMessage.getMediaFile())) {
                continue;
            }

            switch (MessageType.fromValue(roleMessage.getRole())) {
                case USER:
                    UserMessage userMessage = UserMessage.builder().text(roleMessage.getContent()).build();
                    if (MessageTypeEnum.isMedia(chatParams.getMessageType())) {
                        List<Media> mediaList = buildMedia(chatParams.getMessageType(), roleMessage);
                        userMessage = UserMessage.builder().text(roleMessage.getContent()).media(mediaList).build();
                        userMessage.getMetadata().put(DashScopeApiConstants.MESSAGE_FORMAT, chatParams.getMessageType());
                    }

                    messages.add(userMessage);
                    break;
                case SYSTEM:
                    SystemMessage systemMessage = new SystemMessage(roleMessage.getContent());
                    messages.add(0, systemMessage);
                    break;
                case ASSISTANT:
                    AssistantMessage assistantMessage = new AssistantMessage(roleMessage.getContent());
                    messages.add(assistantMessage);
                    break;
                default:
                    break;
            }
        }

        return messages;
    }

    public static List<Media> buildMedia(String messageType, ChatRoleMessage roleMessage) {
        List<String> mediaUrlList = roleMessage.getMediaUrls();
        MultipartFile file = roleMessage.getMediaFile();
        List<Media> mediaList = new ArrayList<>();

        if (!MessageTypeEnum.isMedia(messageType) || (CollectionUtils.isEmpty(mediaUrlList) && Objects.isNull(file))) {
            return mediaList;
        }

        if (Objects.nonNull(file)) {
            Media media = new Media(MimeTypeUtils.parseMimeType(file.getContentType()), file.getResource());
            mediaList.add(media);
            return mediaList;
        }

        try {
            if (MessageTypeEnum.isAudio(messageType)) {
                for (String url : mediaUrlList) {
                    mediaList.add(new Media(MediaType.parseMediaType("audio/mpeg"), new URI(url)));
                }
            } else {
                List<String> imgUrlList = new ArrayList<>();
                if (MessageTypeEnum.VIDEO.name().equals(messageType)) {
                    imgUrlList.addAll(MediaUtil.extractFrames(mediaUrlList.get(0), 5));
                } else if (MessageTypeEnum.IMAGE.name().equals(messageType)) {
                    imgUrlList.addAll(mediaUrlList);
                }

                for (String url : imgUrlList) {
                    mediaList.add(new Media(MimeTypeUtils.IMAGE_PNG, new URI(url)));
                }
            }
        } catch (Exception e) {
            log.error("Media build error, url:{}", JSON.toJSONString(mediaUrlList), e);
        }


        return mediaList;
    }

}
