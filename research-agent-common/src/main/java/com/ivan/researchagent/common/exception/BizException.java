package com.ivan.researchagent.common.exception;

import lombok.Data;

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
public class BizException extends RuntimeException {


    private static final long serialVersionUID = -5779906486373016891L;


    /**
     * 错误码
     */
    private final ErrorInfo errorInfo;


    /**
     * 不允许使用无参数异常
     */
    private BizException() {
        this.errorInfo = null;
    }

    /**
     * 指定错误码构造通用异常
     *
     * @param errorInfo 错误码
     */
    public BizException(final ErrorInfo errorInfo) {
        super(errorInfo.getMessage());
        this.errorInfo = errorInfo;
    }


    /**
     * 构造通用异常
     *
     * @param errorInfo       错误码
     * @param detailedMessage 详细描述
     */
    public BizException(final ErrorInfo errorInfo, final String detailedMessage) {
        super(detailedMessage);
        this.errorInfo = errorInfo;
    }

    /**
     * 构造通用异常
     *
     * @param errorInfo 错误码
     * @param t         导火索
     */
    public BizException(final ErrorInfo errorInfo, final Throwable t) {
        super(errorInfo.getMessage(), t);
        this.errorInfo = errorInfo;
    }


    /**
     * 构造通用异常
     *
     * @param errorInfo       错误码
     * @param detailedMessage 详细描述
     * @param t               导火索
     */
    public BizException(final ErrorInfo errorInfo, final String detailedMessage,
                        final Throwable t) {
        super(detailedMessage, t);
        this.errorInfo = errorInfo;
    }

}
