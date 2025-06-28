package com.ivan.researchagent.common.exception;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/28/周六
 **/
public enum ErrorEnum implements ErrorInfo {

    /**
     * 系统开小差
     */
    SNEAK_AWAY(-1, "系统开小差去了，请您稍后重试"),

    ;


    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 描述
     */
    private final String message;


    /**
     * @param code    错误码
     * @param message 描述
     */
    ErrorEnum(final Integer code, final String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
