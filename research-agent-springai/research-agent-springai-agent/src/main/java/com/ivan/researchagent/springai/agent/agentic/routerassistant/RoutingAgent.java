package com.ivan.researchagent.springai.agent.agentic.routerassistant;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.ivan.researchagent.springai.agent.anno.ToolAgent;
import com.ivan.researchagent.springai.llm.model.chat.ChatRequest;
import com.ivan.researchagent.springai.llm.model.chat.ChatResult;
import com.ivan.researchagent.springai.llm.service.ChatService;
import com.ivan.researchagent.springai.agent.model.tool.AgentRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @author: ivan
 * @since: 2025/1/4 16:15
 */
@Slf4j
@Service
public class RoutingAgent {

    @Resource
    private ChatService chatService;

    @Resource
    private ApplicationContext applicationContext;

    private final String systemPrompt = """
            #角色:
            你是一个专门的路由分发agent，职责是将用户的输入精确地分发给适当的专业agent处理。你需要严格遵循以下规则：
                            
            #职责功能:
            1.命令识别和分类
            2.精确路由分发
            3.保持输入完整性
            
            #工作原则：
            1.不修改用户输入
            - 保持用户输入的原始状态
            - 不添加任何额外解释或修饰
            - 不进行任何预处理或优化
            
            2.快速准确分发
            - 基于关键词和语义进行分类
            - 选择最合适的专业agent
            - 使用标准化的分发格式
            
            3.职责边界
            - 不参与具体任务处理
            - 不提供建议或反馈
            - 仅负责路由分发
            
            #分发规则：
            1.基础格式
            {
                "originalInput": "用户原始输入",
                "targetAgent": "目标agent名称",
                "category": "任务类别",
                "priority": "优先级(1-5)",
                "timestamp": "时间戳"
            }
            
            2.Agent分类表：
            doctor_agent:医生信息查询和操作
            medical_agent: 医疗咨询、健康问题
            finance_agent: 金融、投资、理财
            tech_agent: 技术问题、编程、IT
            education_agent: 教育、学习、考试
            legal_agent: 法律咨询、合同审查
            writing_agent: 写作、文案、创作
            translation_agent: 翻译、语言
            analysis_agent: 数据分析、研究
            assistant_agent: 日常助手、生活服务
            other_agent: 未明确分类的任务
            
            #识别规则：
            1.关键词匹配
            - 优先匹配专业术语和领域特定词汇
            - 考虑上下文语境
            - 支持多语言识别
            
            2.任务分类
            - 基于用户意图分类
            - 考虑任务复杂度
            - 识别任务优先级
            
            3.冲突处理
            - 当存在多个可能的目标agent时，选择最专业的一个
            - 对于复杂任务，可以标记多个相关agent
            - 不确定时归类到other_agent
            
            #执行流程：
            1.接收输入
            2.快速分析关键信息
            3.匹配目标agent
            4.生成分发指令
            5.执行路由分发
            
           # 错误处理：
            1.输入不完整
            - 直接转发到assistant_agent处理
            - 不要尝试补充或修改
            
            2.分类不明确
            - 转发到other_agent
            - 标记为待分类状态
            
            3.系统错误
            - 记录错误信息
            - 转发到system_agent
            
            #禁止行为：
            1.不要修改用户的原始输入
            2.不要添加解释或建议
            3.不要与用户交互或对话
            4.不要进行任何形式的处理或优化
            5.不要存储或缓存用户输入
            
            """;

    private final String systemPrompt1 =  """
            #角色(Role):
            您是一个智能助手,负责根据用户的输入指令进行相关操作。
            
            # 配置(Profile):
            - author: 伊凡Ivan
            - version: 1.0
            - language: 中文 
            - description: 根据用户的输入指令进行相关操作。
            
            ##功能(Skills):
            - 根据用户输入的指令，调用合适的函数处理并返回结果给用户
                        
            ##规则约束(Constrains):
            - 不要对用户输入的指令进行任何修改
            - 输出结果不做任何处理或修改直接返回
            
            """;

    public ChatResult call(ChatRequest chatRequest) {
        buildChatMessage(chatRequest);

        //ChatResult distributionResult =  distributionAgent(chatMessage);
        ChatResult distributionResult =  chatService.chat(chatRequest);

        return distributionResult;
    }

    public Flux<ChatResult> stream(ChatRequest chatRequest) {
        buildChatMessage(chatRequest);

        //Flux<ChatResult> distributionResult = streamDistributionAgent(chatMessage);
        Flux<ChatResult> distributionResult =  chatService.steam(chatRequest)
                .flatMap(chatResult -> {
                    log.info("sessionId:{}, agent router action stream request:{}, result：{}", chatRequest.getSessionId(), chatRequest.getUserMessage(), chatResult.getContent());
                    return Flux.just(chatResult);
                });

        return distributionResult;
    }


    private ChatResult distributionAgent(ChatRequest chatRequest) {
        AgentRequest agentRequest = new AgentRequest();
        agentRequest.setTargetAgent(chatRequest.getAgent());
        agentRequest.setOriginalInput(chatRequest.getUserMessage());
        chatRequest.setUserMessage(JSON.toJSONString(agentRequest));

        ChatResult distributionResult =  chatService.chat(chatRequest);
        String distributionContent = distributionResult.getContent();
        if (distributionContent.contains("targetAgent")) {
            agentRequest = JSON.parseObject(distributionContent, AgentRequest.class);
            chatRequest.setAgent(agentRequest.getTargetAgent());
            chatRequest.setUserMessage(agentRequest.getOriginalInput());

            log.info("sessionId:{}, agent router distribution request:{}, result：{}", chatRequest.getSessionId(), chatRequest.getUserMessage(), distributionContent);
            return distributionAgent(chatRequest);
        } else {
            log.info("sessionId:{}, agent router action request:{}, result：{}", chatRequest.getSessionId(), chatRequest.getUserMessage(), distributionResult.getContent());
            return distributionResult;
        }
    }

    private Flux<ChatResult> streamDistributionAgent(ChatRequest chatRequest) {
        AtomicReference<AgentRequest> agentRequest = new AtomicReference<>(new AgentRequest());
        agentRequest.get().setTargetAgent(chatRequest.getAgent());
        agentRequest.get().setOriginalInput(chatRequest.getUserMessage());
        chatRequest.setUserMessage(JSON.toJSONString(agentRequest));

        Flux<ChatResult> distributionResult =  chatService.steam(chatRequest)
                .flatMap(chatResult -> {
                    String distributionContent = chatResult.getContent();
                    if (distributionContent.contains("targetAgent")) {
                        agentRequest.set(JSON.parseObject(distributionContent, AgentRequest.class));
                        chatRequest.setAgent(agentRequest.get().getTargetAgent());
                        chatRequest.setUserMessage(agentRequest.get().getOriginalInput());

                        log.info("sessionId:{}, agent router distribution stream request:{}, result：{}", chatRequest.getSessionId(), chatRequest.getUserMessage(), distributionContent);
                        return streamDistributionAgent(chatRequest);
                    } else {
                        log.info("sessionId:{}, agent router action stream request:{}, result：{}", chatRequest.getSessionId(), chatRequest.getUserMessage(), chatResult.getContent());
                        return Flux.just(chatResult);
                    }
                });

        return distributionResult;
    }

    private void buildChatMessage(ChatRequest chatRequest) {
        // 如果启用Agent则获取Agent的bean
        if (chatRequest.getEnableAgent()) {
            // 构建系统角色提示词
            chatRequest.setSystemMessage(systemPrompt1);

            // 获取带有Agent注解的bean
            Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(ToolAgent.class);
            List<String> toolBeanNames = beansWithAnnotation.keySet().stream().toList();
            List<String> inputFunctions = Optional.ofNullable(chatRequest.getToolNames()).orElse(Lists.newArrayList());
            List<String> functions = CollectionUtils.union(inputFunctions, toolBeanNames).stream().toList();
            chatRequest.setToolNames(functions);

//            List<ToolCallback> toolCallbacks = new ArrayList<>();
//            for (Map.Entry<String, Object> entry : beansWithAnnotation.entrySet()) {
//                if (entry.getValue() instanceof ToolCallback) {
//                    toolCallbacks.add((ToolCallback) entry.getValue());
//                }
//            }
//            chatMessage.setToolCallBacks(toolCallbacks);
        }
    }
}
