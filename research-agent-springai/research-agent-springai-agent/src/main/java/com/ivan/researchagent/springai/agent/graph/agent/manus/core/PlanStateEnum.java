package com.ivan.researchagent.springai.agent.graph.agent.manus.core;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/21/周四
 **/
public enum PlanStateEnum {
    NOT_STARTED("not_started", "未开始"),
    ACTIVE("active", "激活"),
    COMPLETED("completed", "完成"),
    ;

    private final String key;
    private final String desc;

    PlanStateEnum(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }
}
