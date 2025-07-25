package com.ivan.researchagent.common.dynamic;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Copyright (c) 2024 research-spring-boot.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/7/25/周四
 **/
public class DynamicDataSource extends AbstractRoutingDataSource {

    public DynamicDataSource(DataSource defaultDataSource, Map<Object, Object> targetDataSources){
        super.setDefaultTargetDataSource(defaultDataSource);
        super.setTargetDataSources(targetDataSources);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String typeKey = DataSourceContextHolder.get();

        if (StringUtils.isBlank(typeKey)) {
            return DataSourceEnums.MASTER.getType();
        }

        return typeKey;
    }

}
