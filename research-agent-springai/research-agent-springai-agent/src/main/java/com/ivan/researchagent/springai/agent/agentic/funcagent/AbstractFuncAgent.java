package com.ivan.researchagent.springai.agent.agentic.funcagent;

import com.ivan.researchagent.common.constant.Constant;
import com.ivan.researchagent.springai.llm.model.ChatMessage;
import com.ivan.researchagent.springai.llm.util.ChatMessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @author: ivan
 * @since: 2025/1/4 15:54
 */
@Slf4j
public abstract class AbstractFuncAgent<Req, Context, Resp> implements BiFunction<Req, Context, Resp> {

    /**
     *  获取函数列表
     * @return
     */
    abstract List<String> getFunctions();

    protected ChatClient.ChatClientRequestSpec buildRequestSpec(String userQuery, String systemPrompt, ToolContext toolContext) {
        Map<String, Object> context = toolContext.getContext();
        String sessionId = (String) context.get(Constant.CONVERSANT_ID);
        ChatClient chatClient = (ChatClient) context.get(Constant.CHAT_CLIENT);
        ChatMessage chatMessage = (ChatMessage) context.get(Constant.CHAT_MESSAGE);
        ChatMemory chatMemory = (ChatMemory) context.get(Constant.CHAT_MEMORY);

        // 构建ChatMessage
        chatMessage.setSystemMessage(systemPrompt);
        chatMessage.setUserMessage(userQuery);

        // 构建Prompt
        List<Message> messages = ChatMessageUtil.buildMessages(chatMessage);
        Prompt prompt = new Prompt(messages);

        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt(prompt);

        //对话增强，默认使用MemoryMemoryAdvisor
        requestSpec.advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId)
                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100));

        List<String> functions = getFunctions();
        if(CollectionUtils.isNotEmpty(functions)) {
            //function call
            requestSpec.functions(functions.toArray(new String[functions.size()]));
            requestSpec.toolContext(Map.of(
                    Constant.CONVERSANT_ID, sessionId,
                    Constant.CHAT_CLIENT, chatClient,
                    Constant.CHAT_MESSAGE, chatMessage,
                    Constant.CHAT_MEMORY, chatMemory
            ));
        }

        return requestSpec;
    }
}
