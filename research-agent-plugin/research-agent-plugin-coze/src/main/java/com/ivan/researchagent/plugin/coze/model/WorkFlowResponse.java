package com.ivan.researchagent.plugin.coze.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/12 18:47
 **/
@Data
public class WorkFlowResponse implements Serializable {

    private Integer code;

    private String msg;

    private String data;

    private String cost;

    private Integer token;
}
