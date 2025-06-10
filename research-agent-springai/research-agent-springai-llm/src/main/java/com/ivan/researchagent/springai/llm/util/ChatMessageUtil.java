package com.ivan.researchagent.springai.llm.util;

import com.ivan.researchagent.common.enumerate.MessageTypeEnum;
import com.ivan.researchagent.springai.llm.model.ChatMessage;
import com.ivan.researchagent.common.model.ChatRoleMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.content.Media;
import org.springframework.util.MimeTypeUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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

    public static List<Message> buildMessages(ChatMessage chatMessage) {
        List<Message> messages = new ArrayList<>();
        for (ChatRoleMessage roleMessage : chatMessage.getMessages()) {
            if (StringUtils.isBlank(roleMessage.getContent()) && CollectionUtils.isEmpty(roleMessage.getMediaUrls())) {
                continue;
            }

            switch (MessageType.fromValue(roleMessage.getRole())) {
                case USER:
                    List<Media> mediaList = buildMedia(chatMessage.getMessageType(), roleMessage);
                    UserMessage userMessage = UserMessage.builder().text(roleMessage.getContent()).media(mediaList).build();
                    if (MessageTypeEnum.isMedia(chatMessage.getMessageType())) {
//                        MessageFormat messageFormat = MessageFormat.valueOf(chatMessage.getMessageType());
//                        userMessage.getMetadata().put(DashScopeChatModel.MESSAGE_FORMAT, messageFormat);
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

    public static   List<Media> buildMedia(String messageType, ChatRoleMessage roleMessage) {
        List<Media> mediaList = new ArrayList<>();
        List<String> imgUrlList = new ArrayList<>();
        List<String> mediaUrlList = roleMessage.getMediaUrls();

        if (!MessageTypeEnum.isMedia(messageType) || CollectionUtils.isEmpty(mediaUrlList)) {
            return mediaList;
        }

        try {
            if (MessageTypeEnum.VIDEO.name().equals(messageType)) {
                imgUrlList.addAll(FrameExtraHelper.getVideoPic(mediaUrlList.get(0)));
            }else if (MessageTypeEnum.IMAGE.name().equals(messageType)) {
                imgUrlList.addAll(mediaUrlList);
            }

            for (String url : imgUrlList) {
                mediaList.add(new Media(MimeTypeUtils.IMAGE_PNG, new URI(url)));
            }
        } catch (Exception e) {
            log.error("buildMedia error", e);
        }

        return mediaList;
    }

}
