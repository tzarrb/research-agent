package com.ivan.researchagent.common.aspect.methodlog;

import com.alibaba.fastjson.JSON;
import com.ivan.researchagent.common.aspect.methodlog.annotation.MethodLogAnno;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/7/25/周五
 **/
@Aspect
@Configuration
public class MethodLogAspect {
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 定义切面
     */
    @Pointcut("@annotation(com.ivan.researchagent.common.aspect.methodlog.annotation.MethodLogAnno) "
            + "|| @within(com.ivan.researchagent.common.aspect.methodlog.annotation.MethodLogAnno)")
    public void excudePointcut() { }

    @Before("excudePointcut()")
    public void doBeforeController(JoinPoint point) {
        try {
            // 映射的类名，方法名，参数
            String method = getMethodInfo(point);
            String param = getParamInfo(point);

            StringBuilder methodInfo = new StringBuilder();
            methodInfo.append("开始执行>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
            methodInfo.append(method);
            methodInfo.append(", 参数:");
            methodInfo.append(param);
            logger.info(methodInfo.toString());
        }catch (Exception e){
            logger.warn(e.getMessage(),e);
        }
    }

    /**
     * 在切入点return之后打印结果
     * @param result 响应结果
     * @throws Throwable 可抛出异常
     */
    @AfterReturning(returning = "result", pointcut = "excudePointcut()")
    public void doAfterReturning(JoinPoint point, Object result) {
        try {
            // 映射的类名，方法名，参数
            String method = getMethodInfo(point);
            String param = getParamInfo(point);

            StringBuilder methodInfo = new StringBuilder();
            methodInfo.append("结束执行>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
            methodInfo.append(method);
            methodInfo.append(", 参数:");
            methodInfo.append(param);
            methodInfo.append(", 结果:");
            methodInfo.append(JSON.toJSONString(result));
            logger.debug(methodInfo.toString());
        }catch (Exception e){
            logger.warn(e.getMessage(),e);
        }
    }

    /**
     * 根据切入点获取方法信息及参数列表
     * @param point 切入点
     * @return 切入的方法信息
     */
    private String getMethodInfo(JoinPoint point) {
        try {
            Signature signature = point.getSignature();
            MethodSignature methodSignature = (MethodSignature)signature;

            Method method = methodSignature.getMethod();
            Class<?> targetClass = method.getDeclaringClass();

            String className = targetClass.getSimpleName();
            String methodName = method.getName();

            String desc = "";
            MethodLogAnno logAnnotation = method.getAnnotation(MethodLogAnno.class);
            if(logAnnotation == null){
                logAnnotation = targetClass.getAnnotation(MethodLogAnno.class);
            }
            if(logAnnotation != null){
                desc = logAnnotation.desc();
            }

            return String.format("%s  类:%s, 方法:%s", desc, className, methodName);
        }catch (Exception e){
            logger.warn("MethodLogAspect.getMethodInfo 接口日志拦截获取方法信息失败", e);
        }

        return "类：, 方法：";
    }

    /**
     * 获取参数信息
     * @param point
     * @return
     */
    public String getParamInfo(JoinPoint point){
        try {
            Signature signature = point.getSignature();
            MethodSignature methodSignature = (MethodSignature)signature;

            String[] parameterNames = methodSignature.getParameterNames();
            Object[] argValues = point.getArgs();
            StringBuffer sb = null;
            if (Objects.nonNull(argValues)) {
                sb = new StringBuffer();
                for (int i = 0; i < argValues.length; i++) {
                    String value = "null";
                    if (argValues[i] instanceof Serializable){
                        value = JSON.toJSONString(argValues[i]);
                    }
                    String name = parameterNames != null ? parameterNames[i] : String.valueOf(i);
                    sb.append(name + ": " + value + ",");
                }
            }
            if (null == sb){
                return "";
            }else{
                return sb.toString();
            }
        }catch (Exception e){
            logger.warn("MethodLogAspect.getParamInfo 接口日志拦截获取参数信息失败", e);
        }

        return "";
    }

}
