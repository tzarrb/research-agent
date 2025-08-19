package com.ivan.researchagent.main.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncCommandAction;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.ReflectAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.ivan.researchagent.common.utils.IdUtil;
import com.ivan.researchagent.springai.llm.model.chat.ChatRequest;
import com.ivan.researchagent.springai.llm.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/12/周二
 **/
@Slf4j
@RestController
@RequestMapping("/graph")
@CrossOrigin(origins = "*") // 支持所有来源的跨域请求
@Tag(name = "节点图控制器", description = "节点图控制器")
public class GraphController {

    //@Resource
    private ChatService chatService;

    private ToolCallbackResolver resolver;

    @Resource(name = "expanderTranslateGraph")
    private StateGraph expanderTranslateGraph;

    @Resource(name = "documentProcessingChain")
    private StateGraph documentProcessingChain;

    @Resource(name = "marketAnalysisParallel")
    private StateGraph marketAnalysisParallel;

    @Resource(name = "customerServiceRouting")
    private StateGraph customerServiceRouting;

    @Resource(name = "projectOrchestrator")
    private StateGraph projectOrchestrator;

    @Resource(name = "contentOptimization")
    private StateGraph contentOptimization;


    @Resource(name = "codeGraph")
    private CompiledGraph codeGraph;

    @Resource(name = "travelGraph")
    private CompiledGraph travelGraph;

    public GraphController(ChatService chatService, ToolCallbackResolver resolver) {
        this.chatService = chatService;
        this.resolver = resolver;
    }

    @GetMapping(value = "/expand-translate")
    @Operation(summary = "并行工作流-扩展翻译", description = "返回处理结果")
    public Map<String, Object> expandAndTranslate(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？", required = false) String query,
                                                  @RequestParam(value = "expander_number", defaultValue = "3", required = false) Integer  expanderNumber,
                                                  @RequestParam(value = "translate_language", defaultValue = "english", required = false) String translateLanguage,
                                                  @RequestParam(value = "thread_id", defaultValue = "", required = false) String threadId) throws GraphStateException, GraphRunnerException {
        if (StringUtils.isBlank(threadId)) {
            threadId = IdUtil.nextId().toString();
        }

        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).build();
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("query", query);
        objectMap.put("expander_number", expanderNumber);
        objectMap.put("translate_language", translateLanguage);

        CompiledGraph compiledGraph = expanderTranslateGraph.compile();
        Optional<OverAllState> invoke = compiledGraph.invoke(objectMap, runnableConfig);

        return invoke.map(OverAllState::data).orElse(new HashMap<>());
    }


    @GetMapping(value = "/document-processing")
    @Operation(summary = "链式工作流-文档处理", description = "返回处理结果")
    public Map<String, Object> documentProcessingChain(@RequestParam(value = "input", defaultValue = "车厘子价格持续下降", required = false) String input,
                                                      @RequestParam(value = "thread_id", defaultValue = "", required = false) String threadId) throws GraphStateException, GraphRunnerException {
        RunnableConfig runnableConfig = getRunnableConfig(threadId);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("market_change", input);

        CompiledGraph compiledGraph = documentProcessingChain.compile();
        Optional<OverAllState> invoke = compiledGraph.invoke(objectMap, runnableConfig);

        var messages = invoke.get().value("messages").orElse(Lists.newArrayList());

        return invoke.map(OverAllState::data).orElse(new HashMap<>());
    }

    @GetMapping(value = "/market-analysis")
    @Operation(summary = "并行工作流-市场分析", description = "返回处理结果")
    public Map<String, Object> marketAnalysisParallel(@RequestParam(value = "input", defaultValue = "车厘子价格持续下降", required = false) String input,
                                                    @RequestParam(value = "threadId", defaultValue = "", required = false) String threadId) throws GraphStateException, GraphRunnerException {
        RunnableConfig runnableConfig = getRunnableConfig(threadId);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("market_change", input);

        CompiledGraph compiledGraph = marketAnalysisParallel.compile();
        Optional<OverAllState> invoke = compiledGraph.invoke(objectMap, runnableConfig);

        var messages = invoke.get().value("messages").orElse(Lists.newArrayList());

        return invoke.map(OverAllState::data).orElse(new HashMap<>());
    }

    @GetMapping(value = "/customer-service")
    @Operation(summary = "路由工作流-客服服务", description = "返回处理结果")
    public Map<String, Object> customerServiceRouting(@RequestParam(value = "input", defaultValue = "请帮我写一篇介绍杭州的文章", required = false) String input,
                                                    @RequestParam(value = "threadId", defaultValue = "", required = false) String threadId) throws GraphStateException, GraphRunnerException {
        RunnableConfig runnableConfig = getRunnableConfig(threadId);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("input", input);

        CompiledGraph compiledGraph = customerServiceRouting.compile();
        Optional<OverAllState> invoke = compiledGraph.invoke(objectMap, runnableConfig);

        var messages = invoke.get().value("messages").orElse(Lists.newArrayList());

        return invoke.map(OverAllState::data).orElse(new HashMap<>());
    }

    @GetMapping(value = "/document-orchestrator")
    @Operation(summary = "编排工作-项目文档生成", description = "返回处理结果")
    public Map<String, Object> documentOrchestrator(@RequestParam(value = "input", defaultValue = "请帮我写一篇介绍杭州的文章", required = false) String input,
                                               @RequestParam(value = "threadId", defaultValue = "", required = false) String threadId) throws GraphStateException, GraphRunnerException {
        RunnableConfig runnableConfig = getRunnableConfig(threadId);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("task_description", input);

        CompiledGraph compiledGraph = projectOrchestrator.compile();
        Optional<OverAllState> invoke = compiledGraph.invoke(objectMap, runnableConfig);

        var messages = invoke.get().value("messages").orElse(Lists.newArrayList());

        return invoke.map(OverAllState::data).orElse(new HashMap<>());
    }

    @GetMapping(value = "/content-generate")
    @Operation(summary = "评估优化-内容写作", description = "返回处理结果")
    public Map<String, Object> contentGenerate(@RequestParam(value = "input", defaultValue = "请帮我写一篇介绍杭州的文章", required = true) String input,
                                                  @RequestParam(value = "thread_id", defaultValue = "", required = false) String threadId) throws GraphStateException, GraphRunnerException {
        RunnableConfig runnableConfig = getRunnableConfig(threadId);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("task", input);

        CompiledGraph compiledGraph = contentOptimization.compile();
        Optional<OverAllState> invoke = compiledGraph.invoke(objectMap, runnableConfig);

        var messages = invoke.get().value("messages").orElse(Lists.newArrayList());

        return invoke.map(OverAllState::data).orElse(new HashMap<>());
    }


    @GetMapping("/code-generate")
    @Operation(summary = "自我反思-代码生成优化", description = "返回处理结果")
    public String codeGraph(@RequestParam(value = "input", defaultValue = "", required = true) String input,
                            @RequestParam(value = "thread_id", defaultValue = "", required = false) String threadId) throws GraphRunnerException {
        RunnableConfig runnableConfig = getRunnableConfig(threadId);
        Optional<OverAllState> result = codeGraph.invoke(Map.of("messages", new UserMessage(input)), runnableConfig);
        List<Message> messages = (List<Message>) result.get().value("messages").get();
        // AssistantMessage assistantMessage = (AssistantMessage) messages.get(messages.size() - 1);

        return messages.stream().filter(msg -> msg.getMessageType() == MessageType.ASSISTANT)
                .reduce((first, second) -> second).map(Message::getText).orElseThrow();
    }

    @GetMapping("/travel-plans")
    @Operation(summary = "推理执行-出行计划", description = "返回处理结果")
    public String travelGraph(@RequestParam(value = "input", defaultValue = "", required = true) String input,
                              @RequestParam(value = "threadId", defaultValue = "", required = false) String threadId) throws GraphRunnerException, GraphStateException {
        RunnableConfig runnableConfig = getRunnableConfig(threadId);
        threadId = runnableConfig.threadId().get();
        log.info("{}: travelGraph input: {}", threadId, input);

        CompiledGraph compiledGraph = getTravelGraph(threadId);
        Optional<OverAllState> result = compiledGraph.invoke(Map.of("messages", new UserMessage(input)), runnableConfig);
        List<Message> messages = (List<Message>) result.get().value("messages").get();

        return messages.stream().filter(msg -> msg.getMessageType() == MessageType.ASSISTANT)
                .reduce((first, second) -> second).map(Message::getText).orElseThrow();
    }

    private CompiledGraph getTravelGraph(String threadId) throws GraphStateException {

        ChatRequest chatRequest = ChatRequest.builder()
                .enableMemory(true)
                .enableStream(false)
                .sessionId(threadId)
                .toolNames(Lists.newArrayList("getWeatherFunction"))
                .build();
        ChatClient chatClient= chatService.getchatClient(chatRequest);

        ReactAgent reactAgent = ReactAgent.builder()
                .name("Travel Plan Agent")
                .chatClient(chatClient)
                .resolver(resolver)
                .maxIterations(10)
                .build();
        return reactAgent.getAndCompileGraph();
    }

    private RunnableConfig getRunnableConfig(String threadId) {
        if (StringUtils.isBlank(threadId)) {
            threadId = IdUtil.nextId().toString();
        }

        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).build();
        return runnableConfig;
    }
}
