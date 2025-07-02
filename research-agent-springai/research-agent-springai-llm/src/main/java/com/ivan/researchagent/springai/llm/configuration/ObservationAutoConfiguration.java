package com.ivan.researchagent.springai.llm.configuration;

import com.ivan.researchagent.springai.llm.config.LLMConfig;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.observation.AdvisorObservationContext;
import org.springframework.ai.chat.client.observation.ChatClientObservationContext;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationContext;
import org.springframework.ai.observation.AiOperationMetadata;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.observation.ToolCallingObservationContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/7/1/周二
 **/
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = LLMConfig.PREFIX + "observation", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ObservationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "observationRegistry")
    public ObservationRegistry observationRegistry(ObjectProvider<ObservationHandler<?>> observationHandlerObjectProvider) {
        ObservationRegistry observationRegistry = ObservationRegistry.create();
        ObservationRegistry.ObservationConfig observationConfig = observationRegistry.observationConfig();
        //observationHandlerObjectProvider.orderedStream().forEach(observationConfig::observationHandler);
        observationHandlerObjectProvider.orderedStream().forEach(handler -> {
            Type[] genericInterfaces = handler.getClass().getGenericInterfaces();
            for (Type type : genericInterfaces) {
                if (type instanceof ParameterizedType parameterizedType
                        && parameterizedType.getRawType() instanceof Class<?> clazz
                        && ObservationHandler.class.isAssignableFrom(clazz)) {

                    Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                    log.info("load observation handler, supports context type: {}", actualTypeArgument);
                }
            }

            // 将handler添加到observationRegistry中
            observationConfig.observationHandler(handler);
        });
        return observationRegistry;
    }

    /**
     * 监听chat model调用
     */
    @Bean
    ObservationHandler<ChatModelObservationContext> chatModelObservationContextObservationHandler() {
        log.info("ChatModelObservation start");
        return new ObservationHandler<>() {

            @Override
            public boolean supportsContext(Observation.Context context) {
                return context instanceof ChatModelObservationContext;
            }

            @Override
            public void onStart(ChatModelObservationContext context) {
                AiOperationMetadata operationMetadata = context.getOperationMetadata();
                Prompt request = context.getRequest();
                log.info("🤖ChatModelObservation start: AiOperationMetadata : {}", operationMetadata);
                log.info("🤖ChatModelObservation start: Prompt : {}", request);
            }

            @Override
            public void onStop(ChatModelObservationContext context) {
                ChatResponse response = context.getResponse();
                log.info("🤖ChatModelObservation start: ChatResponse : {}", response);
            }
        };
    }

    /**
     * 监听chat client调用
     */
    @Bean
    ObservationHandler<ChatClientObservationContext> chatClientObservationContextObservationHandler() {
        log.info("ChatClientObservation start");
        return new ObservationHandler<>() {

            @Override
            public boolean supportsContext(Observation.Context context) {
                return context instanceof ChatClientObservationContext;
            }

            @Override
            public void onStart(ChatClientObservationContext context) {
                ChatClientRequest request = context.getRequest();
                List<? extends Advisor> advisors = context.getAdvisors();
                boolean stream = context.isStream();
                log.info("💬ChatClientObservation start: ChatClientRequest : {}, Advisors : {}, stream : {}",
                        request, advisors, stream);
            }

            @Override
            public void onStop(ChatClientObservationContext context) {
                ObservationHandler.super.onStop(context);
            }
        };
    }

    /**
     * 监听工具调用
     */
    @Bean
    public ObservationHandler<AdvisorObservationContext> advisorObservationContextObservationHandler() {
        log.info("AdvisorObservation start");
        return new ObservationHandler<>() {
            @Override
            public boolean supportsContext(Observation.Context context) {
                return context instanceof AdvisorObservationContext;
            }

            @Override
            public void onStart(AdvisorObservationContext context) {
                int order = context.getOrder();
                String advisorName = context.getAdvisorName();
                ChatClientRequest request = context.getChatClientRequest();
                log.info("🔨AdvisorObservation start: {} - {} - {}", order, advisorName, request);
            }

            @Override
            public void onStop(AdvisorObservationContext context) {
                int order = context.getOrder();
                String advisorName = context.getAdvisorName();
                ChatClientResponse response = context.getChatClientResponse();
                log.info("✅AdvisorObservation stop: {} - {} - {}", order, advisorName, response);
            }
        };
    }

    /**
     * 监听工具调用
     */
    @Bean
    public ObservationHandler<ToolCallingObservationContext> toolCallingObservationContextObservationHandler() {
        log.info("ToolCallingObservation start");
        return new ObservationHandler<>() {
            @Override
            public boolean supportsContext(Observation.Context context) {
                return context instanceof ToolCallingObservationContext;
            }

            @Override
            public void onStart(ToolCallingObservationContext context) {
                ToolDefinition toolDefinition = context.getToolDefinition();
                log.info("🔨ToolCallingObservation start: {} - {}", toolDefinition.name(), context.getToolCallArguments());
            }

            @Override
            public void onStop(ToolCallingObservationContext context) {
                ToolDefinition toolDefinition = context.getToolDefinition();
                log.info("✅ToolCallingObservation stop: {} - {}", toolDefinition.name(), context.getToolCallResult());
            }
        };
    }

    /**
     * 监听embedding model调用
     */
    @Bean
    public ObservationHandler<EmbeddingModelObservationContext> embeddingModelObservationContextObservationHandler() {
        log.info("EmbeddingModelObservation start");
        return new ObservationHandler<>() {
            @Override
            public boolean supportsContext(Observation.Context context) {
                return context instanceof EmbeddingModelObservationContext;
            }

            @Override
            public void onStart(EmbeddingModelObservationContext context) {
                log.info("📚EmbeddingModelObservation start: {} - {}", context.getOperationMetadata().operationType(),
                        context.getOperationMetadata().provider());
            }
        };
    }


}
