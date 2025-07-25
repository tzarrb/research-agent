package com.ivan.researchagent.common.dynamic;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Copyright (c) 2024 research-spring-boot.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/12/周四
 **/
@Component
@Aspect
@Order(4)
public class DataSourceAop {

    @Pointcut("@annotation(com.ivan.researchagent.common.dynamic.DS) "
            + "|| @within(com.ivan.researchagent.common.dynamic.DS)")
    public void dynamicDataSource(){}

    @Around("dynamicDataSource()")
    public Object datasourceAround(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature)point.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();

        // 注解方法优先，方法无注解时读取类注解
        DS ds = method.getAnnotation(DS.class);
        if (Objects.isNull(ds)) {
            ds = targetClass.getAnnotation(DS.class);
        }
        if (Objects.nonNull(ds)){
            DataSourceContextHolder.set(ds.value());
        }

        try {
            return point.proceed();
        } finally {
            DataSourceContextHolder.clear();
        }
    }

}
