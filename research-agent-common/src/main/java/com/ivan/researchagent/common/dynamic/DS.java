package com.ivan.researchagent.common.dynamic;

/**
 * Copyright (c) 2024 research-spring-boot.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/7/25/周五
 **/

import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DS {
         String value() default "master";
        }
