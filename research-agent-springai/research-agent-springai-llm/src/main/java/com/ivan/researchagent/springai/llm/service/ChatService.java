package com.ivan.researchagent.springai.llm.service;

import com.alibaba.cloud.ai.advisor.RetrievalRerankAdvisor;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ivan.researchagent.common.constant.Constant;
import com.ivan.researchagent.common.utils.IdUtil;
import com.ivan.researchagent.core.model.ModelOptions;
import com.ivan.researchagent.springai.llm.advisors.ChatMemoryAdvisorSpec;
import com.ivan.researchagent.springai.llm.advisors.ReasoningContentAdvisor;
import com.ivan.researchagent.springai.llm.config.LLMConfig;
import com.ivan.researchagent.springai.llm.model.chat.ChatRequest;
import com.ivan.researchagent.springai.llm.model.chat.ChatResult;
import com.ivan.researchagent.springai.llm.provider.ModelFactory;
import com.ivan.researchagent.springai.llm.util.ChatMessageUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
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
 * @since: 2024/11/28 17:57
 **/
@Slf4j
@Service
public class ChatService implements InitializingBean {

    @Resource
    private ModelFactory modelFactory;

    @Resource
    private ChatMemory chatMemory;

    @Resource
    @Qualifier("vectorStoreRagAdvisor")
    private RetrievalAugmentationAdvisor vectorStoreRagAdvisor;
    @Resource
    @Qualifier("webSearchRagAdvisor")
    private RetrievalAugmentationAdvisor webSearchRagAdvisor;
    @Resource
    private RetrievalRerankAdvisor retrievalRerankAdvisor;
    @Resource
    private ReasoningContentAdvisor reasoningContentAdvisor;

//    @Value("classpath:prompts/system-qa.st")
//    private org.springframework.core.io.Resource systemQaResource;


    @Resource
    private LLMConfig llmConfig;

    private final List<String> REASONER_MODEL = Lists.newArrayList("deepseek-reasoner", "deepseek-r1");

    private final ModelOptions.ModelOptionsBuilder modelOptionsBuilder = ModelOptions.builder()
            .enableMemory(true)
            .enableSearch(true)
            .enableLogging(true);

    private final String systemPrompt =  """
            你是智能助理，针对用户的提问你可以通过工具或联网获取对应的信息，并回答给用户，回答的语气要拟人化，真实自然，并使用中文；
            
            """;


    @Override
    public void afterPropertiesSet() throws Exception {

    }

    /**
     * 获取ChatClient
     * @param chatRequest
     * @return
     */
    public ChatClient getchatClient(ChatRequest chatRequest) {
        if (StringUtils.isBlank(chatRequest.getProvider())) {
            chatRequest.setProvider(llmConfig.getDefaultProvider());
        }
        if (StringUtils.isBlank(chatRequest.getModel())) {
            chatRequest.setModel(llmConfig.getDefaultModel());
        }
        modelOptionsBuilder
                .provider(chatRequest.getProvider())
                .model(chatRequest.getModel());

        ModelOptions modelOptions = modelOptionsBuilder
                //.defaultSystem(chatRequest.getDefaultSystem())
                ///.defaultUser(chatRequest.findUserMessage())
                .conversantId(chatRequest.getSessionId())
                .enableMemory(chatRequest.getEnableMemory())
                .enableStream(chatRequest.getEnableStream())
                .enableSearch(chatRequest.getEnableWeb())
                .formatType(chatRequest.getFormatType())
                .defaultTools(chatRequest.getDefaultTools())
                .defaultToolNames(chatRequest.getDefaultToolNames())
                .defaultToolCallbacks(chatRequest.getDefaultToolCallbacks())
                .defaultToolCallbackProviders(chatRequest.getDefaultToolCallbackProviders())
                .build();

        ChatClient chatClient = modelFactory.get(modelOptions);
        return chatClient;
    }

    /**
     * 构建对话请求
     * @param chatRequest
     * @return
     */
    private ChatClient.ChatClientRequestSpec buildRequestSpec(ChatRequest chatRequest) {
        //对话会话的唯一标识
        String sessionId = chatRequest.getSessionId();
        if (StringUtils.isBlank(sessionId)) {
            sessionId = IdUtil.nextId().toString();
            chatRequest.setSessionId(sessionId);
            log.info("sessionId:{}", sessionId);
        }

        List<Message> messages = ChatMessageUtil.buildMessages(chatRequest);
        Prompt prompt = new Prompt(messages);

        ChatClient chatClient = getchatClient(chatRequest);
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt(prompt);

        if (chatRequest.getEnableMemory()) {
            //对话记忆的唯一标识
            String conversantId = chatRequest.getSessionId();

            //对话增强，默认使用 MemoryAdvisor
            requestSpec.advisors(new ChatMemoryAdvisorSpec(conversantId));
        }
        //本地文档搜索增强
        if(chatRequest.getEnableLocal()) {
            //RAG检索增强生成
            //requestSpec.advisors(new QuestionAnswerAdvisor(vectorStore));
            //requestSpec.advisors(retrievalRerankAdvisor);

            // RAG检索增强生成
            requestSpec.advisors(vectorStoreRagAdvisor);
        }

        // 网络搜索增强
        if(chatRequest.getEnableWeb()) {
            requestSpec.advisors(webSearchRagAdvisor);
        }

        //
        if (chatRequest.getEnableThink()) {
            // 深度思考增强
            requestSpec.advisors(reasoningContentAdvisor);
        }

        //tool call
        if (CollectionUtils.isNotEmpty(chatRequest.getTools())) {
            requestSpec.tools(chatRequest.getTools().toArray(new Object[0]));
        }

        //tool call
        if (CollectionUtils.isNotEmpty(chatRequest.getToolNames())) {
            requestSpec.toolNames(chatRequest.getToolNames().toArray(new String[0]));
        }

        //tool callback
        if (CollectionUtils.isNotEmpty(chatRequest.getToolCallBacks())) {
            requestSpec.toolCallbacks(chatRequest.getToolCallBacks());
        }

        // tool callback provider
        if (CollectionUtils.isNotEmpty(chatRequest.getToolCallbackProviders())) {
            requestSpec.toolCallbacks(chatRequest.getToolCallbackProviders().toArray(new ToolCallbackProvider[0]));
        }

        if (CollectionUtils.isNotEmpty(chatRequest.getTools())
                || CollectionUtils.isNotEmpty(chatRequest.getToolNames())
                || CollectionUtils.isNotEmpty(chatRequest.getToolCallBacks())
                || CollectionUtils.isNotEmpty(chatRequest.getToolCallbackProviders())) {
            Map<String, Object> toolContext = Maps.newHashMap();
            toolContext.put(Constant.CONVERSANT_ID, sessionId);
            toolContext.put(Constant.CHAT_CLIENT, chatClient);
            toolContext.put(Constant.CHAT_MEMORY, chatMemory);
            toolContext.put(Constant.ORIGINAL_INPUT, chatRequest.findUserMessage());
            if (chatRequest.getEnableAgent()) {
                toolContext.put(Constant.CHAT_MESSAGE, chatRequest);
            }
            requestSpec.toolContext(toolContext);
        }

        //参数动态配置，如model、maxTokens、presencePenalty、responseFormat、stream、streamOptions、seed、stop、temperature、topP、tools、toolChoice
        //requestSpec.options();

        return requestSpec;
    }

    public String chat(String input) {
        ChatClient chatClient = modelFactory.get(modelOptionsBuilder.build());
        ChatResponse response = chatClient.prompt()
                .user(input)
                .call()
                .chatResponse();
        return response.getResult().getOutput().getText();
    }

    public ChatResponse chat(Prompt prompt) {
        ChatClient chatClient = modelFactory.get(modelOptionsBuilder.build());
        ChatResponse response = chatClient.prompt(prompt)
                .call()
                .chatResponse();
        return response;
    }

    public ChatResult chat(ChatRequest chatRequest) {
        ChatClient.ChatClientRequestSpec requestSpec = buildRequestSpec(chatRequest);

        ChatResponse response = requestSpec.call().chatResponse();
        AssistantMessage assistantMessage = response.getResult().getOutput();
        ChatResult chatResult = new ChatResult();
        chatResult.setSessionId(chatRequest.getSessionId());
        chatResult.setChatResponse(response);
        chatResult.setContent(assistantMessage.getText());
        if (REASONER_MODEL.contains(chatRequest.getModel())) {
            DeepSeekAssistantMessage deepSeekAssistantMessage = (DeepSeekAssistantMessage) assistantMessage;
            chatResult.setReasoningContent(deepSeekAssistantMessage.getReasoningContent());
        }
        log.info("sessionId:{}, chat content：{}, response: {}", chatRequest.getSessionId(), chatResult.getContent(), response);
        return chatResult;
    }

    public Flux<ChatResult> steam(String input) {
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setProvider("dashscope");
        chatRequest.setModel("qwen-max");
        chatRequest.addUserMessage(input);
        chatRequest.addSystemMessage(systemPrompt);
        chatRequest.setEnableMemory(true);
        chatRequest.setEnableStream(true);
        chatRequest.setEnableAgent(false);

        return steam(chatRequest);
    }

    public Flux<ChatResult> steam(ChatRequest chatRequest) {
        ChatClient.ChatClientRequestSpec requestSpec = buildRequestSpec(chatRequest);
        return requestSpec.stream().chatResponse().map(chatResponse -> {
            String content = "";
            Generation generation = chatResponse.getResult();
            if (ObjectUtils.isNotEmpty(generation)) {
                content = generation.getOutput().getText();
            } else {
                content = "模型无结果输出!!!";
                log.warn("sessionId:{}, stream request received null generation", chatRequest.getSessionId());
            }

            if (ObjectUtils.isEmpty(content)) {
                content = "模型无内容生成!!!"; // 根据业务需求设置默认内容
                log.warn("sessionId:{}, Received null content, using default content", chatRequest.getSessionId());
            }
            log.info("sessionId:{}, stream chat content：{}, response: {}", chatRequest.getSessionId(), content, JSON.toJSONString(chatResponse));

            ChatResult chatResult = new ChatResult();
            chatResult.setSessionId(chatRequest.getSessionId());
            chatResult.setChatResponse(chatResponse);
            chatResult.setContent(content);

            return chatResult;
        }).onErrorResume(error -> {
            log.error("sessionId:{}, Error in stream chat: ", chatRequest.getSessionId(), error);
            ChatResult chatResult = new ChatResult();
            chatResult.setSessionId(chatRequest.getSessionId());
            chatResult.setContent(error.getMessage());

            return Mono.just(chatResult);
        });
    }

    public Flux<ChatResponse> steamChat(ChatRequest chatRequest) {
        ChatClient.ChatClientRequestSpec requestSpec = buildRequestSpec(chatRequest);
        return requestSpec.stream().chatResponse();
    }

    public SseEmitter sseChat(ChatRequest chatRequest) {
        SseEmitter sseEmitter = new SseEmitter();
        ChatClient.ChatClientRequestSpec requestSpec = buildRequestSpec(chatRequest);
        requestSpec
                .stream()
                .chatResponse()
                .subscribe(
                        chunk -> {
                            try {
                                Map<String, Object> result = new HashMap<String, Object>();
                                result.put("sessionId", chatRequest.getSessionId());

                                Generation generation = chunk.getResult();
                                if (ObjectUtils.isNotEmpty(generation)) {
                                    String content = generation.getOutput().getText();
                                    if (StringUtils.isNotBlank(content)) {
                                        result.put("content", content);
                                        log.info("sessionId:{}, stream chat content: {}, response: {}",
                                                chatRequest.getSessionId(), content, JSON.toJSONString(chunk));
                                    }

                                    String reasoningContent = String.valueOf(generation.getOutput().getMetadata().get("reasoningContent"));
                                    if (StringUtils.isNotBlank(reasoningContent)) {
                                        result.put("reasoningContent", reasoningContent);
                                        log.info("sessionId:{}, stream chat content: {}, reasoningContent:{}, response: {}",
                                                chatRequest.getSessionId(), content, reasoningContent, JSON.toJSONString(chunk));
                                    }
                                } else  {
                                    result.put("content", "模型无结果输出!!!");
                                    log.warn("sessionId:{}, sse request received null generation", chatRequest.getSessionId());
                                }

                                // 发送消息到客户端
                                // 注意这里，我们直接发送 JSON 字符串，让 SseEmitter 自动添加 data: 前缀
                                sseEmitter.send(result);
                            } catch (IOException e) {
                                sseEmitter.completeWithError(e);
                            }
                        },
                        error -> {
                            try {
                                sseEmitter.send(Map.of("content", "发生错误: " + error.getMessage()));
                                log.error("sessionId:{}, Error in stream chat: ", chatRequest.getSessionId(), error);
                            } catch (IOException e) {
                                sseEmitter.completeWithError(e);
                            } finally {
                                sseEmitter.complete();
                            }
                        },
                        sseEmitter::complete);

        return sseEmitter;
    }

    public <T> T chatObject(String input, Class<T> clazz) {
        ChatClient chatClient = modelFactory.get(modelOptionsBuilder.build());
        return chatClient.prompt()
                .user(input)
                .call()
                .entity(clazz);
    }

    public <T> List<T> chatArray(String input) {
        ChatClient chatClient = modelFactory.get(modelOptionsBuilder.build());
        return chatClient.prompt()
                .user(input)
                .call()
                .entity(new ParameterizedTypeReference<List<T>>() {});
    }

    public String functionChat(String input, List<String> toolNames) {
        ChatClient chatClient = modelFactory.get(modelOptionsBuilder.build());
        ChatResponse response = chatClient.prompt()
                .toolNames(toolNames.toArray(new String[0]))
                .user(input)
                .call()
                .chatResponse();
        return response.getResult().getOutput().getText();
    }

    public <T> T toolChatObject(String input, List<String> toolNames, Class<T> clazz) {
        ChatClient chatClient = modelFactory.get(modelOptionsBuilder.build());
        return chatClient.prompt()
                .toolNames(toolNames.toArray(new String[0]))
                .user(input)
                .call()
                .entity(clazz);
    }

    public <T> List<T> toolChatArray(String input, List<String> functionNames) {
        ChatClient chatClient = modelFactory.get(modelOptionsBuilder.build());
        return chatClient.prompt()
                .toolNames(functionNames.toArray(new String[0]))
                .user(input)
                .call()
                .entity(new ParameterizedTypeReference<List<T>>() {});
    }

}
