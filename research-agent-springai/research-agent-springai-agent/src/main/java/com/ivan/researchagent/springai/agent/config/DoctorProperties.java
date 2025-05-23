package com.ivan.researchagent.springai.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/16 13:38
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "doctor")
public class DoctorProperties implements Serializable {

    /**
     * 令牌
     */
    private String token;

    /**
     * 医供者页面地址
     */
    private String providerPageUrl;

    /**
     * 医供者注销地址
     */
    private String providerCancelUrl;
}
