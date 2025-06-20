package com.ivan.researchagent.main.controller;

import com.ivan.researchagent.common.utils.StringTemplateUtil;
import com.ivan.researchagent.main.model.vo.ArticleVO;
import com.ivan.researchagent.springai.llm.model.chat.ChatMessage;
import com.ivan.researchagent.springai.llm.model.chat.ChatResult;
import com.ivan.researchagent.springai.llm.service.ChatService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/9/周一
 **/
@Slf4j
@RestController
@RequestMapping("/format/chat")
public class StructuredOutputController {

    @Resource
    private ChatService chatService;

    private ChatClient chatClient;

    private MapOutputConverter mapConverter;
    private ListOutputConverter listConverter;
    private BeanOutputConverter<ArticleVO> beanConverter;

    private String beanFormat;

    @PostConstruct
    public void init() {
        // map转换器 文章
        this.mapConverter = new MapOutputConverter();
        // list转换器
        this.listConverter = new ListOutputConverter(new DefaultConversionService());

        // bean转换器
        this.beanConverter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<ArticleVO>() {
                }
        );

        this.beanFormat = beanConverter.getFormat();

        chatClient = chatService.getchatClient(ChatMessage.builder().enableMemory(false).build());
    }

    @GetMapping("/map")
    public Map<String, Object> chatMap(@RequestParam(value = "query", defaultValue = "请为我描述下影子的特性") String query) {
        String promptUserSpec = """
                format: key为描述的东西，value为对应的值
                outputExample: {format};
                """;
        String format = mapConverter.getFormat();
        log.info("map format: {}",format);

//        String result = chatClient.prompt(query)
//                .user(u -> u.text(promptUserSpec)
//                        .param("format", format))
//                .call().content();

        String template = query + "{format}";
        String queryMsg = StringTemplateUtil.render(template, Map.of("format", format));
        String result = chatService.chat(queryMsg);

        log.info("format map result: {}", result);
        assert result != null;
        Map<String, Object> convert = null;
        try {
            convert = mapConverter.convert(result);
            log.info("反序列成功，convert: {}", convert);
        } catch (Exception e) {
            log.error("反序列化失败");
        }
        return convert;
    }

    @GetMapping("/list")
    public List<String> chatList(@RequestParam(value = "query", defaultValue = "请为我描述下影子的特性") String query) {
        String promptUserSpec = """
                format: value为对应的值
                outputExample: {format};
                """;
        String format = listConverter.getFormat();
        log.info("list format: {}",format);

//        String result = chatClient.prompt(query)
//                .user(u -> u.text(promptUserSpec)
//                        .param("format", format))
//                .call().content();

        String template = query + "{format}";
        String queryMsg = StringTemplateUtil.render(template, Map.of("format", format));
        String result = chatService.chat(queryMsg);

        log.info("format list result: {}", result);
        assert result != null;
        List<String> convert = null;
        try {
            convert = listConverter.convert(result);
            log.info("反序列成功，convert: {}", convert);
        } catch (Exception e) {
            log.error("反序列化失败");
        }
        return convert;
    }

    @GetMapping("/json")
    public String simpleChatFormat(@RequestParam(value = "query", defaultValue = "请以JSON格式介绍你自己") String query) {
//        ChatClient chatClientJson = chatService.getchatClient(ChatMessage.builder().enableMemory(false).formatType("json").build());
//        return chatClientJson.prompt(query)
//                .call().content();

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setFormatType("json");
        chatMessage.setUserMessage(query);
        ChatResult result = chatService.chat(chatMessage);
        log.info("format json result: {}", result.getContent());
        return result.getContent();
    }

    @GetMapping("/bean")
    public String chatModel(@RequestParam(value = "query", defaultValue = "以影子为作者，写一篇200字左右的有关人工智能诗篇") String query) {
        String template = query + "{format}";

//        PromptTemplate promptTemplate = PromptTemplate.builder()
//                .template(template)
//                .variables(Map.of("format", beanFormat))
//                .renderer(StTemplateRenderer.builder().build())
//                .build();
//
//        Prompt prompt = promptTemplate.create();
//
//        String result = chatClient.prompt(prompt).call().content();

        String queryMsg = StringTemplateUtil.render(template, Map.of("format", beanFormat));
        String result = chatService.chat(queryMsg);

        log.info("result: {}", result);
        assert result != null;
        try {
            ArticleVO convert = beanConverter.convert(result);
            log.info("反序列成功，convert: {}", convert);
        } catch (Exception e) {
            log.error("反序列化失败");
        }
        return result;
    }

    @GetMapping("/bean-format")
    public String ChatFormat(@RequestParam(value = "query", defaultValue = "以影子为作者，写一篇200字左右的有关人工智能诗篇") String query) {
        String promptUserSpec = """
                format: 以纯文本输出 json，请不要包含任何多余的文字——包括 markdown 格式;
                outputExample: {format};
                """;
        String result = chatClient.prompt(query)
                .user(u -> u.text(promptUserSpec)
                        .param("format", beanFormat))
                .call().content();

        log.info("result: {}", result);
        assert result != null;
        try {
            ArticleVO convert = beanConverter.convert(result);
            log.info("反序列成功，convert: {}", convert);
        } catch (Exception e) {
            log.error("反序列化失败");
        }
        return result;
    }

    /**
     * @return {@link ArticleVO}
     */
    @GetMapping("/bean-play")
    public ArticleVO simpleChat(HttpServletResponse response) {
        Flux<String> flux = this.chatClient.prompt()
                .user(u -> u.text("""
						requirement: 请用大概 120 字，作者为 牧生 ，为计算机的发展历史写一首现代诗;
						format: 以纯文本输出 json，请不要包含任何多余的文字——包括 markdown 格式;
						outputExample: {
							 "title": {title},
							 "author": {author},
							 "date": {date},
							 "content": {content}
						};
						"""))
                .stream()
                .content();

        String result = String.join("\n", Objects.requireNonNull(flux.collectList().block()))
                .replaceAll("\\n", "")
                .replaceAll("\\s+", " ")
                .replaceAll("\"\\s*:", "\":")
                .replaceAll(":\\s*\"", ":\"");

        log.info("LLMs 响应的 json 数据为：{}", result);

        return beanConverter.convert(result);
    }
}
