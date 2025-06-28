package com.ivan.researchagent.springai.llm.provider;

import com.alibaba.cloud.ai.dashscope.api.DashScopeResponseFormat;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.ivan.researchagent.common.enumerate.FormatTypeEnum;
import com.ivan.researchagent.common.enumerate.LLMTypeEnum;
import com.ivan.researchagent.common.enumerate.MessageTypeEnum;
import com.ivan.researchagent.common.model.ModelOptions;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.ResponseFormat;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/11/28 17:02
 **/
@Component
public class ModelFactory {

    @Resource
    private OpenAiChatModel openAiChatModel;
    //@Resource
    //private AnthropicChatModel anthropicChatModel;
    //@Resource
    //private VertexAiGeminiChatModel geminiChatModel;
    @Resource
    private OllamaChatModel ollamaChatModel;
    @Resource
    private DeepSeekChatModel deepSeekChatModel;
    @Resource
    private DashScopeChatModel dashScopeChatModel;
    //@Resource
    //private QianFanChatModel qianFanChatModel;

    //@Resource
    //private OpenAiApi baseOpenAiApi;

    @Resource(name = "redisMessageChatMemoryAdvisor")
    private MessageChatMemoryAdvisor redisMessageChatMemoryAdvisor;

    @Resource(name = "redisPromptChatMemoryAdvisor")
    private PromptChatMemoryAdvisor redisPromptChatMemoryAdvisor;

    @Value("${spring.ai.dashscope.multi.options.model:qwen-vl-max-latest}")
    private String multiModel;

    private DashScopeResponseFormat responseFormat;

    private final Map<String, ChatClient> chatClientMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // AI模型内置支持JSON模式
        this.responseFormat = DashScopeResponseFormat.builder().type(DashScopeResponseFormat.Type.JSON_OBJECT).build();
    }

    public ChatClient get(final ModelOptions modelOptions) {
        return createChatClient(modelOptions);

//        if (StringUtils.isBlank(modelOptions.getConversantId())) {
//            return createChatClient(modelOptions);
//        }
//
//        String chatClientKey = modelOptions.getConversantId() + modelOptions.getMessageType();
//        return chatClientMap.computeIfAbsent(chatClientKey, s -> createChatClient(modelOptions));
    }

    private ChatClient createChatClient(ModelOptions modelOptions) {
        ChatModel chatModel;
        ChatOptions chatOptions;
        boolean formatJson = FormatTypeEnum.isJson(modelOptions.getFormatType());
        switch (LLMTypeEnum.valueOf(modelOptions.getProvider().toUpperCase())) {
            case DASHSCOPE:
                chatModel = dashScopeChatModel;
                DashScopeChatOptions dashScopeChatOptions = (DashScopeChatOptions)dashScopeChatModel.getDefaultOptions();
                if (StringUtils.isNotBlank(modelOptions.getModel())) {
                    dashScopeChatOptions.setModel(modelOptions.getModel());
                }
                if(MessageTypeEnum.isMedia(modelOptions.getMessageType())) {
                    //多模态
                    dashScopeChatOptions.setModel(multiModel);
                    dashScopeChatOptions.setMultiModel(true);
                }
                //设置流式对话
                dashScopeChatOptions.setStream(modelOptions.getEnableStream());

                //模型在生成文本时是否使用互联网搜索结果进行参考。取值如下：
                //true：启用互联网搜索，模型会将搜索结果作为文本生成过程中的参考信息，但模型会基于其内部逻辑判断是否使用互联网搜索结果。
                //如果模型没有搜索互联网，建议优化Prompt。
                //false（默认）：关闭互联网搜索。
                dashScopeChatOptions.setEnableSearch(modelOptions.getEnableSearch());

                //返回内容的格式。可选值：{"type": "text"}或{"type": "json_object"}
                if (formatJson) {
                    dashScopeChatOptions.setResponseFormat(DashScopeResponseFormat.builder().type(DashScopeResponseFormat.Type.JSON_OBJECT).build());
                }

                //采样温度，控制模型生成文本的多样性。
                //temperature越高，生成的文本更多样，反之，生成的文本更确定。
                //取值范围： [0, 2)
                //由于temperature与top_p均可以控制生成文本的多样性，因此建议您只设置其中一个值
                //dashScopeChatOptions.setTemperature(0.7);

                //核采样的概率阈值，控制模型生成文本的多样性。
                //top_p越高，生成的文本更多样。反之，生成的文本更确定。
                //取值范围：（0,1.0]
                //dashScopeChatOptions.setTopP(0.8);

                //控制模型生成文本时的内容重复度。
                //取值范围：[-2.0, 2.0]。正数会减少重复度，负数会增加重复度。
                //适用场景：
                //较高的presence_penalty适用于要求多样性、趣味性或创造性的场景，如创意写作或头脑风暴。
                //较低的presence_penalty适用于要求一致性或专业术语的场景，如技术文档或其他正式文档。
                //dashScopeChatOptions.setRepetitionPenalty(1.5);

                //设置seed参数会使文本生成过程更具有确定性，通常用于使模型每次运行的结果一致。
                //在每次模型调用时传入相同的seed值（由您指定），并保持其他参数不变，模型将尽可能返回相同的结果。
                //取值范围：0到231−1。
                //dashScopeChatOptions.setSeed(1234);

                //使用stop参数后，当模型生成的文本即将包含指定的字符串或token_id时，将自动停止生成。
                //您可以在stop参数中传入敏感词来控制模型的输出。
                //stop为array类型时，不可以将token_id和字符串同时作为元素输入
                //dashScopeChatOptions.setStop(List.of("Human:", "AI:"));

                //如果您希望对于某一类问题，大模型能够采取制定好的工具选择策略（如强制使用某个工具、强制使用至少一个工具、强制不使用工具等），可以通过修改tool_choice参数来强制指定工具调用的策略。可选值：
                //"auto" 表示由大模型进行工具策略的选择。
                //"required" 如果您希望无论输入什么问题，Function Calling 都可以进行工具调用，可以设定tool_choice参数为"required"；
                //"none" 如果您希望无论输入什么问题，Function Calling 都不会进行工具调用，可以设定tool_choice参数为"none"；
                //{"type": "function", "function": {"name": "the_function_to_call"}} 如果您希望对于某一类问题，Function Calling 能够强制调用某个工具
                //dashScopeChatOptions.setToolChoice("required");

                chatOptions = dashScopeChatOptions;
                break;
//            case QIANFAN:
//                chatModel = qianFanChatModel;
//                QianFanChatOptions qianFanChatOptions = (QianFanChatOptions)qianFanChatModel.getDefaultOptions();
//                if (StringUtils.isNotBlank(modelOptions.getModel())) {
//                    qianFanChatOptions.setModel(modelOptions.getModel());
//                }
//                //qianFanChatOptions.setResponseFormat(new QianFanApi.ChatCompletionRequest.ResponseFormat("json_object"));
//                chatOptions =qianFanChatOptions;
//                break;
            case DEEPSEEK:
                chatModel = deepSeekChatModel;
                DeepSeekChatOptions deepSeekChatOptions = (DeepSeekChatOptions)deepSeekChatModel.getDefaultOptions();
                if (StringUtils.isNotBlank(modelOptions.getModel())) {
                    deepSeekChatOptions.setModel(modelOptions.getModel());
                }
                //返回内容的格式。可选值：{"type": "text"}或{"type": "json_object"}
                if (formatJson) {
                    deepSeekChatOptions.setResponseFormat(ResponseFormat.builder().type(ResponseFormat.Type.JSON_OBJECT).build());
                }

                chatOptions = deepSeekChatOptions;
                break;
            case OPENAI:
            case CLAUDE:
            case GEMINI:
                chatModel = openAiChatModel;
                OpenAiChatOptions openAiChatOptions = (OpenAiChatOptions)openAiChatModel.getDefaultOptions();
                if (StringUtils.isNotBlank(modelOptions.getModel())) {
                    openAiChatOptions.setModel(modelOptions.getModel());
                }
                openAiChatOptions.setStreamUsage(modelOptions.getEnableStream());
                //返回内容的格式。可选值：{"type": "text"}或{"type": "json_object"}
                if (formatJson) {
                    openAiChatOptions.setResponseFormat(org.springframework.ai.openai.api.ResponseFormat.builder().type(org.springframework.ai.openai.api.ResponseFormat.Type.JSON_OBJECT).build());
                }

                chatOptions = openAiChatOptions;
                break;
//            case GEMINI:
//                // 为 Gemini 派生新的 OpenAiApi
//                OpenAiApi geminiApi = baseOpenAiApi.mutate()
//                        .baseUrl(System.getenv("spring.ai.gemini.base-url"))
//                        .apiKey(System.getenv("spring.ai.gemini.api-key"))
//                        .build();
//                chatModel = openAiChatModel.mutate().openAiApi(geminiApi).build();
//                OpenAiChatOptions geminiChatOptions = (OpenAiChatOptions)openAiChatModel.getDefaultOptions();
//                if (StringUtils.isNotBlank(modelOptions.getModel())) {
//                    geminiChatOptions.setModel(modelOptions.getModel());
//                }
//                geminiChatOptions.setStreamUsage(modelOptions.getEnableStream());
//                //返回内容的格式。可选值：{"type": "text"}或{"type": "json_object"}
//                if (formatJson) {
//                    geminiChatOptions.setResponseFormat(org.springframework.ai.openai.api.ResponseFormat.builder().type(org.springframework.ai.openai.api.ResponseFormat.Type.JSON_OBJECT).build());
//                }
//
//                chatOptions = geminiChatOptions;
//                break;
            case OLLAMA:
                chatModel = ollamaChatModel;
                OllamaOptions ollamaOptions = (OllamaOptions)ollamaChatModel.getDefaultOptions();
                if (StringUtils.isNotBlank(modelOptions.getModel())) {
                    ollamaOptions.setModel(modelOptions.getModel());
                }
                //返回内容的格式。可选值：{"type": "text"}或{"type": "json_object"}
                //ollamaOptions.setFormat();

                chatOptions = ollamaOptions;
                break;
            default:
                throw new IllegalArgumentException("Unsupported LLM type: " + modelOptions.getProvider());
        }

        ChatClient.Builder builder = ChatClient.builder(chatModel);
        //默认配置
        builder.defaultOptions(chatOptions);

        if (modelOptions.getEnableMemory()) {
            //初始化基于内存的对话记忆
            builder.defaultAdvisors(redisPromptChatMemoryAdvisor);
        }
        if (modelOptions.getEnableLogging()) {
            //启用日志记录，org.springframework.ai.chat.client.advisor=DEBUG
            //builder.defaultAdvisors(new LoggingAdvisor());
        }

        if (StringUtils.isNotBlank(modelOptions.getSystemText())) {
            //系统提示词
            builder.defaultSystem(modelOptions.getSystemText());
        }
        if (StringUtils.isNotBlank(modelOptions.getUserText())) {
            //用户提示词
            builder.defaultUser(modelOptions.getUserText());
        }

        ChatClient chatClient = builder.build();
        return chatClient;
    }
}
