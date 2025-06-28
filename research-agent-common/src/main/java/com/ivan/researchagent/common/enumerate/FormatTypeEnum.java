package com.ivan.researchagent.common.enumerate;

import org.apache.commons.lang3.StringUtils;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/6/周五
 **/
public enum FormatTypeEnum {
    BEAN("bean", "模型格式"),
    LIST("list", "列表格式"),
    MAP("map", "map格式"),
    JSON("json", "json格式"),
    ;

    private final String key;
    private final String desc;

    FormatTypeEnum(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }

    public static FormatTypeEnum getByKey(String key) {
        for (FormatTypeEnum value : values()) {
            if (value.getKey().equals(key)) {
                return value;
            }
        }
        return null;
    }

    public static boolean isBean(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        return BEAN.key.equals(key);
    }

    public static boolean isList(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        return LIST.key.equals(key);
    }

    public static boolean isMap(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        return MAP.key.equals(key);
    }

    public static boolean isJson(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        return JSON.key.equals(key);
    }
}
