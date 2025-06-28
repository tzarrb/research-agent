package com.ivan.researchagent.common.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/28/周六
 **/
@Data
public class RestResult<T> implements Serializable {

    private static final Integer SUCCESS_CODE = 0;

    private Integer code;
    private String message;
    private String error;
    private String exception;
    private String path;
    private Date timestamp;
    private T data;

    public RestResult() {

    }

    public RestResult(T t) {
        this.code = SUCCESS_CODE;
        this.setData(t);
    }

    public RestResult(final int code, final String message, final T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static RestResult success() {
        return get(SUCCESS_CODE, null, new Object());
    }

    public static <T> RestResult<T> success(final T data) {
        return get(SUCCESS_CODE, null, data);
    }

    public static <T> RestResult<T> success(final String msg, final T data) {
        return get(SUCCESS_CODE, msg, data);
    }

    private static <T> RestResult<T> get(final int code, final String msg, final T data) {
        return new RestResult<>(code, msg, data);
    }
}
