package com.ivan.researchagent.common.model;

import lombok.Data;

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
public class ErrorResponse {

    private Integer code;
    private String message;
    private String exception;
    private String path;
    private Date timestamp;
}
