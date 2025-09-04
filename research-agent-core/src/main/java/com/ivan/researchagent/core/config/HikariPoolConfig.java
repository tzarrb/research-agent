package com.ivan.researchagent.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/7/25/周五
 **/
@Data
@Component
@ConfigurationProperties(prefix = "spring.datasource.hikari")
public class HikariPoolConfig {
    private volatile long connectionTimeout;
    private volatile long idleTimeout;
    private volatile long maxLifetime;
    private volatile int  maximumPoolSize;
    private volatile int minimumIdle;
    private boolean isAutoCommit;

}
