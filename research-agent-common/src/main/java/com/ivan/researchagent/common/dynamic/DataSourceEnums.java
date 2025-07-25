package com.ivan.researchagent.common.dynamic;

/**
 * Copyright (c) 2024 research-spring-boot.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/7/25/周四
 **/
public enum DataSourceEnums {

    /**
     * 主库
     */
    MASTER("master", "主库"),

    /**
     * 从库
     */
    SALVE("salve", "从库"),


    /**
     * MySQL库
     */
    MYSQL("mysql", "MySQL库"),

    /**
     * PostgreSQL库
     */
    POSTGRESQL("postgresql", "PostgreSQL库"),

    ;

    private String type;
    private String name;


    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    DataSourceEnums(String type, String name) {
        this.type = type;
        this.name = name;
    }
}
