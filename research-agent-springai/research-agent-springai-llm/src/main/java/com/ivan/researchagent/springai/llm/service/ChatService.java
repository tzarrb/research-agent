package com.ivan.researchagent.springai.llm.service;

import com.alibaba.cloud.ai.advisor.RetrievalRerankAdvisor;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ivan.researchagent.common.constant.Constant;
import com.ivan.researchagent.common.enumerate.MessageTypeEnum;
import com.ivan.researchagent.common.utils.IdUtil;
import com.ivan.researchagent.core.model.ModelOptions;
import com.ivan.researchagent.springai.llm.advisors.ChatMemoryAdvisorSpec;
import com.ivan.researchagent.springai.llm.advisors.ReasoningContentAdvisor;
import com.ivan.researchagent.springai.llm.config.LLMConfig;
import com.ivan.researchagent.springai.llm.model.chat.ChatParams;
import com.ivan.researchagent.springai.llm.model.chat.ChatResult;
import com.ivan.researchagent.springai.llm.provider.ModelFactory;
import com.ivan.researchagent.springai.llm.util.ChatMessageUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
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
import java.util.Objects;

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
     * @param chatParams
     * @return
     */
    public ChatClient getChatClient(ChatParams chatParams) {
        if (StringUtils.isBlank(chatParams.getProvider())) {
            chatParams.setProvider(llmConfig.getDefaultProvider());
        }
        if (StringUtils.isBlank(chatParams.getModel())) {
            if (MessageTypeEnum.isMedia(chatParams.getMessageType())) {
                chatParams.setModel(llmConfig.getDefaultMultiModel());
            } else if (BooleanUtils.isTrue(chatParams.getEnableThink())) {
                chatParams.setModel(llmConfig.getDefaultThinkModel());
            } else {
                chatParams.setModel(llmConfig.getDefaultModel());
            }
        }
        modelOptionsBuilder
                .provider(chatParams.getProvider())
                .model(chatParams.getModel());

        ModelOptions modelOptions = modelOptionsBuilder
                .defaultSystem(chatParams.getDefaultSystem())
                ///.defaultUser(chatRequest.findUserMessage())
                .conversantId(chatParams.getSessionId())
                .enableMemory(chatParams.getEnableMemory())
                .enableStream(chatParams.getEnableStream())
                .enableMulti(chatParams.getEnableMulti())
                .enableSearch(chatParams.getEnableWeb())
                .formatType(chatParams.getFormatType())
                .defaultTools(chatParams.getDefaultTools())
                .defaultToolNames(chatParams.getDefaultToolNames())
                .defaultToolCallbacks(chatParams.getDefaultToolCallbacks())
                .defaultToolCallbackProviders(chatParams.getDefaultToolCallbackProviders())
                .build();

        ChatClient chatClient = modelFactory.get(modelOptions);
        return chatClient;
    }

    /**
     * 构建对话请求
     * @param chatParams
     * @return
     */
    private ChatClient.ChatClientRequestSpec buildRequestSpec(ChatParams chatParams) {
        //对话会话的唯一标识
        String sessionId = chatParams.getSessionId();
        if (StringUtils.isBlank(sessionId)) {
            sessionId = IdUtil.nextId().toString();
            chatParams.setSessionId(sessionId);
            log.info("生成新的sessionId:{}", sessionId);
        }

        List<Message> messages = ChatMessageUtil.buildMessages(chatParams);
        Prompt prompt = new Prompt(messages);

        ChatClient chatClient = getChatClient(chatParams);
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt(prompt);

        if (chatParams.getEnableMemory()) {
            //对话记忆的唯一标识
            String conversantId = chatParams.getSessionId();

            //对话增强，默认使用 MemoryAdvisor
            requestSpec.advisors(new ChatMemoryAdvisorSpec(conversantId));
        }
        //本地文档搜索增强
        if(chatParams.getEnableLocal()) {
            //RAG检索增强生成
            //requestSpec.advisors(new QuestionAnswerAdvisor(vectorStore));
            //requestSpec.advisors(retrievalRerankAdvisor);

            // RAG检索增强生成
            requestSpec.advisors(vectorStoreRagAdvisor);
        }

        // 网络搜索增强
        if(chatParams.getEnableWeb()) {
            requestSpec.advisors(webSearchRagAdvisor);
        }

        // 深度思考增强
        if (chatParams.getEnableThink()) {
            //requestSpec.advisors(reasoningContentAdvisor);
        }

        //tool call
        if (CollectionUtils.isNotEmpty(chatParams.getTools())) {
            requestSpec.tools(chatParams.getTools().toArray(new Object[0]));
        }

        //tool call
        if (CollectionUtils.isNotEmpty(chatParams.getToolNames())) {
            requestSpec.toolNames(chatParams.getToolNames().toArray(new String[0]));
        }

        //tool callback
        if (CollectionUtils.isNotEmpty(chatParams.getToolCallBacks())) {
            requestSpec.toolCallbacks(chatParams.getToolCallBacks());
        }

        // tool callback provider
        if (CollectionUtils.isNotEmpty(chatParams.getToolCallbackProviders())) {
            requestSpec.toolCallbacks(chatParams.getToolCallbackProviders().toArray(new ToolCallbackProvider[0]));
        }

        if (CollectionUtils.isNotEmpty(chatParams.getTools())
                || CollectionUtils.isNotEmpty(chatParams.getToolNames())
                || CollectionUtils.isNotEmpty(chatParams.getToolCallBacks())
                || CollectionUtils.isNotEmpty(chatParams.getToolCallbackProviders())) {
            Map<String, Object> toolContext = Maps.newHashMap();
            toolContext.put(Constant.CONVERSANT_ID, sessionId);
            toolContext.put(Constant.CHAT_CLIENT, chatClient);
            toolContext.put(Constant.CHAT_MEMORY, chatMemory);
            toolContext.put(Constant.ORIGINAL_INPUT, chatParams.findUserMessage());
            if (chatParams.getEnableAgent()) {
                toolContext.put(Constant.CHAT_MESSAGE, chatParams);
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

    public ChatResult chat(ChatParams chatParams) {
        ChatClient.ChatClientRequestSpec requestSpec = buildRequestSpec(chatParams);

        ChatResponse response = requestSpec.call().chatResponse();
        AssistantMessage assistantMessage = response.getResult().getOutput();
        ChatResult chatResult = new ChatResult();
        chatResult.setConversantId(chatParams.getSessionId());
        //chatResult.setChatResponse(response);
        chatResult.setContent(assistantMessage.getText());
        chatResult.setReasoningContent(getReasoningContent(chatParams, assistantMessage));
        chatResult.setSearchResult(getSearchInfo(chatParams, assistantMessage));
        log.info("Chat result: sessionId:{}, content：{}, response: {}", chatParams.getSessionId(), chatResult.getContent(), response);
        return chatResult;
    }

    public Flux<ChatResult> steam(String input) {
        ChatParams chatParams = new ChatParams();
        chatParams.addUserMessage(input);
        chatParams.addSystemMessage(systemPrompt);
        chatParams.setEnableMemory(true);
        chatParams.setEnableStream(true);
        chatParams.setEnableAgent(false);

        return steam(chatParams);
    }

    public Flux<ChatResult> steam(ChatParams chatParams) {
       log.info("开始执行流式聊天，sessionId: {}, provider: {}, model: {}, enableStream: {}",
               chatParams.getSessionId(), chatParams.getProvider(), chatParams.getModel(), chatParams.getEnableStream());
       
       ChatClient.ChatClientRequestSpec requestSpec = buildRequestSpec(chatParams);
       log.info("构建请求规格完成，sessionId: {}", chatParams.getSessionId());
       
       return requestSpec.stream().chatResponse()
           .doOnSubscribe(subscription -> {
               log.info("开始订阅流式响应，sessionId: {}", chatParams.getSessionId());
           })
           .map(chatResponse -> {
               log.debug("大模型返回结果，sessionId: {}, response:{}", chatParams.getSessionId(), chatResponse);
               ChatResult chatResult = new ChatResult();
               chatResult.setConversantId(chatParams.getSessionId());
               //chatResult.setChatResponse(chatResponse);

               String content = "";
               Generation generation = chatResponse.getResult();
               if (ObjectUtils.isNotEmpty(generation)) {
                   AssistantMessage assistantMessage = generation.getOutput();
                   if (Objects.nonNull(assistantMessage)) {
                       content = assistantMessage.getText();
                       // 推理内容
                       String reasoningContent = getReasoningContent(chatParams, assistantMessage);
                       chatResult.setReasoningContent(reasoningContent);
                       // 搜索结构
                       Object searchResult = getSearchInfo(chatParams, assistantMessage);
                       chatResult.setSearchResult(searchResult);

                       log.debug("Stream chat result: sessionId: {}, content: {}, reasoningContent:{}, searchResult:{}",
                               chatParams.getSessionId(), content, reasoningContent, searchResult);
                   } else {
                       content = "模型返回的结果没有AI助手信息!!!";
                       log.warn("sessionId:{}, Generation存在但Output为空", chatParams.getSessionId());
                   }
               } else {
                   content = "模型无结果返回!!!";
                   log.warn("sessionId:{}, stream request received null generation", chatParams.getSessionId());
               }

               chatResult.setContent(content);
               return chatResult;
           })
           .doOnError(error -> {
               log.error("流式聊天过程中发生错误，sessionId: {}, error: ", chatParams.getSessionId(), error);
           })
           .doOnComplete(() -> {
               log.info("流式聊天完成，sessionId: {}", chatParams.getSessionId());
           })
           .onErrorResume(error -> {
               log.error("sessionId:{}, Error in stream chat: ", chatParams.getSessionId(), error);
               ChatResult chatResult = new ChatResult();
               chatResult.setConversantId(chatParams.getSessionId());
               chatResult.setContent("发生错误: " + error.getMessage());

               return Mono.just(chatResult);
           });
    }

    public Flux<ChatResponse> steamChat(ChatParams chatParams) {
        ChatClient.ChatClientRequestSpec requestSpec = buildRequestSpec(chatParams);
        return requestSpec.stream().chatResponse();
    }

    public SseEmitter sseChat(ChatParams chatParams) {
        SseEmitter sseEmitter = new SseEmitter();
        ChatClient.ChatClientRequestSpec requestSpec = buildRequestSpec(chatParams);
        requestSpec
                .stream()
                .chatResponse()
                .subscribe(
                        chunk -> {
                            try {
                                log.debug("大模型返回结果，sessionId: {}, response:{}", chatParams.getSessionId(), chunk);
                                Map<String, Object> result = new HashMap<String, Object>();
                                result.put("sessionId", chatParams.getSessionId());

                                Generation generation = chunk.getResult();
                                if (Objects.nonNull(generation)) {
                                    AssistantMessage assistantMessage = generation.getOutput();
                                    String content = assistantMessage.getText();
                                    if (StringUtils.isEmpty(content)) {
                                        content = "";
                                    }
                                    result.put("content", content);

                                    log.info("sessionId:{}, sse chat content: {}, response: {}",
                                            chatParams.getSessionId(), content, JSON.toJSONString(chunk));

                                    String reasoningContent = getReasoningContent(chatParams, assistantMessage);
                                    result.put("reasoningContent", reasoningContent);

                                    Object searchResult = getSearchInfo(chatParams, assistantMessage);
                                    result.put("searchResult", searchResult);

                                    log.debug("SSE chat result: sessionId: {}, content: {}, reasoningContent:{}, searchResult:{}",
                                            chatParams.getSessionId(), content, reasoningContent, searchResult);
                                } else  {
                                    result.put("content", "模型无结果返回!!!");
                                    log.warn("sessionId:{}, sse request received null generation", chatParams.getSessionId());
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
                                log.error("sessionId:{}, Error in stream chat: ", chatParams.getSessionId(), error);
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

    private String getReasoningContent(ChatParams chatParams, AssistantMessage assistantMessage) {
        if (!REASONER_MODEL.contains(chatParams.getModel())) {
            return "";
        }

        String reasoningContent = "";
        if (assistantMessage instanceof DeepSeekAssistantMessage) {
            DeepSeekAssistantMessage deepSeekAssistantMessage = (DeepSeekAssistantMessage) assistantMessage;
            reasoningContent = deepSeekAssistantMessage.getReasoningContent();
        } else {
            reasoningContent = String.valueOf(assistantMessage.getMetadata().get("reasoningContent"));
        }

        log.info("sessionId:{}, chat reasoning model content: {}, message: {}",
                chatParams.getSessionId(), reasoningContent, JSON.toJSONString(assistantMessage));

        return reasoningContent;
    }

    private Object getSearchInfo(ChatParams chatParams, AssistantMessage assistantMessage) {
        if (!BooleanUtils.isTrue(chatParams.getEnableWeb())) {
            return "";
        }

        Object searchResult = assistantMessage.getMetadata().get("search_info");
        log.info("sessionId:{}, sse chat search info: {}, message: {}",
                chatParams.getSessionId(), JSON.toJSON(searchResult), JSON.toJSONString(assistantMessage));
        return searchResult;
    }
}
