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
public interface ErrorInfo {

    /**
     * 系统常数
     */
    int SYSTEM_CONSTANT = 1000;

    /**
     * 获取错误码
     *
     * @return : 错误码
     */
    Integer getCode();

    /**
     * 获取错误信息
     *
     * @return : 错误信息
     */
    String getMessage();

    /**
     * 业务对应模块错误码
     *
     * @return : 模块错误码，系统错误码默认值为0
     */
    default Integer getModuleCode() {
        return 0;
    }

    /**
     * 异常场景下获取错误code
     *
     * @return : 错误信息
     */
    default Integer getErrorCode() {
        return Integer.parseInt(getModuleCode() * SYSTEM_CONSTANT + getCode() + "");
    }

    /**
     * 异常场景下获取错误信息
     *
     * @param errorMsg 支持后续拼接自定义错误
     * @return : 错误信息
     */
    default String getErrorMsg(String errorMsg) {
        return "" + getMessage() + errorMsg;
    }

}
