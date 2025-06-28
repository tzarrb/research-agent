package com.ivan.researchagent.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/6/周五
 **/
@Slf4j
public class StringTemplateUtil {

    /**
     * 使用命名参数替换模板占位符
     * @param template 字符串模板（示例："Hello {name}, age: {age}"）
     * @param params 参数映射表
     * @return 渲染后的字符串
     */
    public static String render(String template, Map<String, Object> params) {
        if (template == null || template.isEmpty()) return "";

        try {
            Pattern pattern = Pattern.compile("\\{(\\w+)\\}");
            Matcher matcher = pattern.matcher(template);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String key = matcher.group(1);
                Object value = params.getOrDefault(key, "");
                matcher.appendReplacement(sb, Matcher.quoteReplacement(handleValue(value)));
            }
            matcher.appendTail(sb);

            return sb.toString();
        } catch (Exception e) {
            log.warn("render error, template:{}, params:{}", template, params, e);
        }
        return template;
    }

    /**
     * 使用顺序参数替换模板占位符
     * @param template 字符串模板（示例："Hello {0}, age: {1}"）
     * @param args 参数数组
     * @return 渲染后的字符串
     */
    public static String render(String template, Object... args) {
        if (template == null || template.isEmpty()) return "";

        try {
            Pattern pattern = Pattern.compile("\\{(\\d+)\\}");
            Matcher matcher = pattern.matcher(template);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                int index = Integer.parseInt(matcher.group(1));
                Object value = (index < args.length) ? args[index] : "";
                matcher.appendReplacement(sb, Matcher.quoteReplacement(handleValue(value)));
            }
            matcher.appendTail(sb);

            return sb.toString();
        } catch (Exception e) {
            log.warn("render error, template:{}, params:{}", template, args, e);
        }

        return template;
    }

    private static String handleValue(Object value) {
        if (value == null) return "";
        if (value instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd").format((Date) value);
        }
        return value.toString();
    }

    // 支持自定义空值占位
//    public static String renderWithDefault(String template, String defaultValue, Object... args) {
//        // 实现逻辑类似，处理null值时使用defaultValue
//    }
}
