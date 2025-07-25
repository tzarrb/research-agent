package com.ivan.researchagent.common.dynamic;

import lombok.extern.slf4j.Slf4j;

/**
 * Copyright (c) 2024 research-spring-boot.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/12/周四
 **/
@Slf4j
public class DataSourceContextHolder {

    private static final ThreadLocal<String> LOCAL = new ThreadLocal<String>();

    public static ThreadLocal<String> getLocal() {
        return LOCAL;
    }

    /**
     * 设置数据源
     * @param dataSourceName 数据源名称
     */
    public static void set(String dataSourceName){
        LOCAL.set(dataSourceName);
    }

    public static String get() {
        return LOCAL.get();
    }

    /**
     * 清除数据源名
     */
    public static void clear() {
        LOCAL.remove();
    }

}
