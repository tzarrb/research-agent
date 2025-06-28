package com.ivan.researchagent.springai.agent.agentic.tool;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/4/7/周一
 **/
public abstract class AbstractToolAgent implements ToolCallback {

    @Override
    public ToolMetadata getToolMetadata() {
        return ToolMetadata.builder().returnDirect(true).build();
    }

    @Override
    public String call(String toolInput) {
        return "";
    }

    /**
     *  获取工具列表
     * @return
     */
    //abstract List<Object> getTools();

//    protected ChatClient.ChatClientRequestSpec buildRequestSpec(String userQuery, String systemPrompt, ToolContext toolContext) {
//        Map<String, Object> context = toolContext.getContext();
//        String sessionId = (String) context.get(Constant.CONVERSANT_ID);
//        ChatClient chatClient = (ChatClient) context.get(Constant.CHAT_CLIENT);
//        ChatMessage chatMessage = (ChatMessage) context.get(Constant.CHAT_MESSAGE);
//        ChatMemory chatMemory = (ChatMemory) context.get(Constant.CHAT_MEMORY);
//
//        // 构建ChatMessage
//        chatMessage.setSystemMessage(systemPrompt);
//        chatMessage.setUserMessage(userQuery);
//
//        // 构建Prompt
//        List<Message> messages = ChatMessageUtil.buildMessages(chatMessage);
//        Prompt prompt = new Prompt(messages);
//
//        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt(prompt);
//
//        //对话增强，默认使用MemoryMemoryAdvisor
//        requestSpec.advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId)
//                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100));
//
//        List<Object> tools = getTools();
//        if(CollectionUtils.isNotEmpty(tools)) {
//            //function call
//            requestSpec.functions(tools.toArray(new String[tools.size()]));
//            requestSpec.toolContext(Map.of(
//                    Constant.CONVERSANT_ID, sessionId,
//                    Constant.CHAT_CLIENT, chatClient,
//                    Constant.CHAT_MESSAGE, chatMessage,
//                    Constant.CHAT_MEMORY, chatMemory
//            ));
//        }
//
//        return requestSpec;
//    }
}
