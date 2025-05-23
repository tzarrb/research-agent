package com.ivan.researchagent.plugin.coze.function;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.ivan.researchagent.plugin.coze.config.CozeProperties;
import com.ivan.researchagent.plugin.coze.model.WorkFlowResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/12 17:53
 **/
@Slf4j
public class TravelingPlannerFunction implements Function<TravelingPlannerFunction.RequestParam, TravelingPlannerFunction.ResponseParam> {

    private static final String TRAVELLING_PLANNER_NAME = "traveling-planner";
    private CozeProperties properties;
    private final WebClient webClient;

    public TravelingPlannerFunction(CozeProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, HttpHeaders.USER_AGENT)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .defaultHeader(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9,ja;q=0.8")
                .defaultHeader("Authorization", "Bearer "+ properties.getAccessToken())
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
                .build();
    }

    public ResponseParam apply(RequestParam request) {
        Map<String, Object> params = new HashMap<>();
        try {
            params.put("workflow_id", properties.getTravelingPlanner());
            params.put("parameters", request);
            Mono<WorkFlowResponse> responseMono = webClient.post().uri(properties.getWorkFlowBaseUrl()).bodyValue(params).retrieve().bodyToMono(WorkFlowResponse.class);
            WorkFlowResponse jsonResponse = responseMono.block();
            assert jsonResponse != null;

            log.info("Traveling planner fetched successfully for param: {}, result:{}", JSON.toJSONString(params), JSON.toJSONString(jsonResponse));

            if (StringUtils.isNotBlank(jsonResponse.getData())) {
                JSONObject responseMap = JSON.parseObject(jsonResponse.getData());
                return new ResponseParam(responseMap.getString("data"));
            } else {
                return new ResponseParam(jsonResponse.getMsg());
            }
        }
        catch (Exception e) {
            log.error("Failed to fetch Traveling planner param: {}", JSON.toJSONString(params), e);
            return new ResponseParam(e.getMessage());
        }
    }

    @JsonClassDescription("根据出发地和目的地查询旅行计划")
    public record RequestParam(
            @JsonProperty(required = false, value = "BOT_USER_INPUT") @JsonPropertyDescription("用户输入") String BOT_USER_INPUT,
            @JsonProperty(required = true, value = "departure") @JsonPropertyDescription("出发城市地址,例如长春市") String departure,
            @JsonProperty(required = true, value = "destination") @JsonPropertyDescription("到达城市,例如北京市") String destination,
            @JsonProperty(required = false, value = "people_num") @JsonPropertyDescription("人数,例如2人") Integer people_num,
            @JsonProperty(required = false, value = "days_num") @JsonPropertyDescription("天数,例如7天") Integer days_num,
            @JsonProperty(required = false, value = "start_date") @JsonPropertyDescription("开始日期,例如2024-12-12") String start_date
    ) {}

    @JsonClassDescription("旅行计划返回结果")
    public record ResponseParam(String content) {}
}
