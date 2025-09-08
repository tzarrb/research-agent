package com.ivan.researchagent.springai.llm.configuration;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/9/5/周五
 **/
@Configuration
public class HttpConfiguration {

    @Value("${http.client.connect-timeout:600}")
    private Long connectTimeout;

    @Value("${http.client.read-timeout:600}")
    private Long readTimeout;
    
//    @Bean
//    public RestClient okHttpRestClient() {
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .connectTimeout(Duration.ofMinutes(10))
//                .readTimeout(Duration.ofMinutes(10))
//                .writeTimeout(Duration.ofMinutes(10)) // 补充写入超时
//                .retryOnConnectionFailure(true) // 启用连接失败自动重试
//                .build();
//
//        return RestClient.builder()
//                .requestFactory(new OkHttp3ClientHttpRequestFactory(okHttpClient))
//                .build();
//    }

    @Bean
    public RestClient.Builder restClientBuilder() {

        // 2. 创建 RequestConfig 并设置超时
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.of(connectTimeout, TimeUnit.SECONDS)) // 设置连接超时
                .setResponseTimeout(Timeout.of(readTimeout, TimeUnit.SECONDS))
                .setConnectionRequestTimeout(Timeout.of(connectTimeout, TimeUnit.SECONDS))
                .build();

        // 3. 创建 CloseableHttpClient 并应用配置
        HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();

        // 4. 使用 HttpComponentsClientHttpRequestFactory 包装 HttpClient
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        // 5. 创建 RestClient 并设置请求工厂
        return RestClient.builder().requestFactory(requestFactory);
    }

}
