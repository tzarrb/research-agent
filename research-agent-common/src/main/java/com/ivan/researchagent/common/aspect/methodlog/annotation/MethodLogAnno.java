package com.ivan.researchagent.common.aspect.methodlog.annotation;

import java.lang.annotation.*;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/7/25/周五
 **/
@Target({ElementType.TYPE,ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MethodLogAnno {
    String desc() default "未知操作";
}
