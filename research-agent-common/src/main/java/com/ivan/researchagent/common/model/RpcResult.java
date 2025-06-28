package com.ivan.researchagent.common.model;

import com.ivan.researchagent.common.exception.ErrorEnum;
import lombok.Data;

import java.io.Serializable;

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
public class RpcResult<T> implements Serializable {

    private static final Long serialVersionUID = -7853376217570690314L;

    private static final Integer SUCCESS_CODE = 0;

    /**
     * 判断返回成功或失败
     */
    private Boolean success;

    /**
     * 返回错误码
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String msg;

    /**
     * 传输数据对象
     */
    private T result;


    public RpcResult() {
        this.success = true;
        this.setCode(SUCCESS_CODE);

    }

    public RpcResult(T t) {
        this.success = true;
        this.setResult(t);
        this.setCode(SUCCESS_CODE);
    }

    public RpcResult(Boolean success, Integer code, String msg, T t) {
        this.success = success;
        this.setResult(t);
        this.setCode(code);
        this.setMsg(msg);
    }

    public static <T> RpcResult<T> success() {
        return get(true, SUCCESS_CODE, null, null);
    }

    public static <T> RpcResult<T> success(final T data) {
        return get(true, SUCCESS_CODE, null, data);
    }

    public static <T> RpcResult<T> error(final int code, final String msg) {
        return get(false, code, msg, null);
    }

    public static <T> RpcResult<T> internalError() {
        return get(false, ErrorEnum.SNEAK_AWAY.getCode(), ErrorEnum.SNEAK_AWAY.getMessage(), null);
    }

    private static <T> RpcResult<T> get(final boolean success, final int code, final String msg, final T data) {
        return new RpcResult<>(success, code, msg, data);
    }
}
