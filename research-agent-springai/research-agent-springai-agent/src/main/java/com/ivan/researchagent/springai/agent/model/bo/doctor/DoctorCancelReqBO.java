package com.ivan.researchagent.springai.agent.model.bo.doctor;

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
 * @since: 2024/12/18 10:49
 **/
@Data
public class DoctorCancelReqBO implements Serializable {
    /**
     * 医供用户ID
     */
    private Long providerId;
    /**
     * 是否检查订单，0-否，1-是
     */
    private Integer ifCheckOrder;
    /**
     * 是否同步SaaS，0-否，1-是
     */
    private Integer ifSyncSaaS;
    /**
     * 是否同步用户
     */
    private Integer ifSyncUser;
}
