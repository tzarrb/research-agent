package com.ivan.researchagent.springai.llm.config;

import com.ivan.researchagent.common.dynamic.DataSourceEnums;
import com.ivan.researchagent.common.dynamic.DynamicDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/7/25/周五
 **/
@Configuration
@MapperScan(basePackages = "com.ivan.researchagent.**.dao", sqlSessionFactoryRef = "SqlSessionFactory")
public class DataSourceConfig {

    @Autowired
    HikariPoolConfig hikariPoolConfig;

    @Primary
    @Bean(name = "postgresqlDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.druid.postgresql")
    public DataSource postgresqlDataSource() {
        HikariDataSource hikariDataSource = DataSourceBuilder.create().type(HikariDataSource.class).build();
        setDsPoolsProperties(hikariDataSource,"postgresql_hikari_pool");
        return hikariDataSource;
    }

    @Bean(name = "mysqlDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.druid.mysql")
    public DataSource mysqlDataSource() {
        HikariDataSource hikariDataSource = DataSourceBuilder.create().type(HikariDataSource.class).build();
        setDsPoolsProperties(hikariDataSource,"mysql_hikari_pool");
        return hikariDataSource;
    }

    @Bean(name = "dynamicDataSource")
    public DynamicDataSource DataSource(@Qualifier("mysqlDataSource") DataSource mysqlDataSource,
                                        @Qualifier("postgresqlDataSource") DataSource postgresqlDataSource) {
        Map<Object, Object> targetDataSource = new HashMap<>();
        targetDataSource.put(DataSourceEnums.MYSQL.getType(), mysqlDataSource);
        targetDataSource.put(DataSourceEnums.POSTGRESQL.getType(), postgresqlDataSource);

        DynamicDataSource dataSource = new DynamicDataSource(postgresqlDataSource, targetDataSource);
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager(@Qualifier("dynamicDataSource") DataSource dynamicDataSource) {
        return new DataSourceTransactionManager(dynamicDataSource);
    }

    @Bean(name = "SqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dynamicDataSource") DataSource dynamicDataSource)  throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dynamicDataSource);

//        bean.setConfigLocation(new ClassPathResource("mybatis/mybatis-config.xml"));
//
//        //配置别名扫描包路径
//        bean.setTypeAliasesPackage("com.ivan.researchagent.springai.server.model");
//
//        //配置mapper的扫描，找到所有的mapper.xml映射文件
//        Resource[] resourcesMapper = new PathMatchingResourcePatternResolver().getResources("classpath:/mybatis/mapper/*.xml");
//        Resource[] resourcesExt = new PathMatchingResourcePatternResolver().getResources("classpath:/mybatis/ext/*.xml");
//        Resource[] resources = ArrayUtils.addAll(resourcesMapper, resourcesExt);
//        bean.setMapperLocations(resources);

        return bean.getObject();
    }

    //设置连接池
    private  void setDsPoolsProperties(HikariDataSource hikariDataSource,String poolName){
        hikariDataSource.setPoolName(poolName);
        if(hikariPoolConfig.getConnectionTimeout() >0) {
            hikariDataSource.setConnectionTimeout(hikariPoolConfig.getConnectionTimeout());
        }
        if(hikariPoolConfig.getIdleTimeout() >0) {
            hikariDataSource.setIdleTimeout(hikariPoolConfig.getIdleTimeout());
        }
        if(hikariPoolConfig.getMaximumPoolSize() >0) {
            hikariDataSource.setMaximumPoolSize(hikariPoolConfig.getMaximumPoolSize());
        }
        if(hikariPoolConfig.getMinimumIdle() >0) {
            hikariDataSource.setMinimumIdle(hikariPoolConfig.getMinimumIdle());
        }
        if(hikariPoolConfig.getMaxLifetime() >0) {
            hikariDataSource.setMaxLifetime(hikariPoolConfig.getMaxLifetime());
        }         if(!hikariPoolConfig.isAutoCommit()) {
            // 默认 true
            hikariDataSource.setAutoCommit(hikariPoolConfig.isAutoCommit());
        }
    }

    @Bean(name = "mysqlJdbcTemplate")
    public JdbcTemplate mysqlJdbcTemplate(@Qualifier("mysqlDataSource") DataSource dataSource) {
        JdbcTemplate mysqlJdbcTemplate = new JdbcTemplate(dataSource);
        return mysqlJdbcTemplate;
    }

    @Bean(name = "postgresqlJdbcTemplate")
    public JdbcTemplate bookJdbcTemplate(@Qualifier("postgresqlDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
