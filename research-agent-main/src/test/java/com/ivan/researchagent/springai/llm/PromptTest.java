package com.ivan.researchagent.springai.llm;

import org.junit.Test;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/9/8/周一
 **/
public class PromptTest {

    @Test
    public void test(){
        testTemplate();
    }

    @Test
    public void testTemplate(){
        String templateText = """
            用户信息：
            姓名：{name}
            {#if age}
            年龄：{age}岁
            {/if}
            {#if premium}
            会员等级：高级会员
            {#else}
            会员等级：普通用户
            {/if}
            
            {#if interests}
            兴趣爱好：
            {#each interests as interest}
            - {interest}
            {/each}
            {/if}
        """;

        String templateText1 = """
            用户信息：
            姓名：{name}
            年龄：{age}岁
        """;

        PromptTemplate template = new PromptTemplate(templateText1);

        Map<String, Object> model = Map.of(
                "name", "李四",
                "age", 25,
                "premium", true,
                "interests", List.of("编程", "音乐", "旅行")
        );

        String result = template.render(model);
        System.out.println(result);
    }

}
