package com.ivan.researchagent.springai.agent.service.doctor;

import com.alibaba.fastjson.JSON;
import com.ivan.researchagent.springai.agent.config.DoctorProperties;
import com.ivan.researchagent.springai.agent.enums.DoctorOperateType;
import com.ivan.researchagent.springai.agent.model.bo.doctor.DoctorCancelReqBO;
import com.ivan.researchagent.springai.agent.model.bo.doctor.DoctorInfoBO;
import com.ivan.researchagent.springai.agent.model.tool.doctor.DoctorOperateRequest;
import com.ivan.researchagent.springai.agent.model.tool.doctor.DoctorQueryRequest;
import com.ivan.researchagent.springai.agent.model.tool.doctor.DoctorUpdateRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/02 16:38
 **/
@Slf4j
@Service
public class DoctorService {

    @Resource
    private DoctorProperties properties;

    private static WebClient.Builder webClientBuilder = WebClient.builder()
            .defaultHeader(HttpHeaders.USER_AGENT, HttpHeaders.USER_AGENT)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
            .defaultHeader(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9,ja;q=0.8")
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024));


    private WebClient buildWebClient() {
        return webClientBuilder.defaultHeader("x-weimai-token", properties.getToken()).build();
    }

    public List<DoctorInfoBO> queryDoctor(DoctorQueryRequest request) {
        //log.info("listMedicalProvider param:{}", JSON.toJSONString(request));
        List<DoctorInfoBO> doctorInfoBOList = new ArrayList<>();
        if (StringUtils.isAllBlank(request.getName(), request.getMobile())) {
            return doctorInfoBOList;
        }

        DoctorInfoBO doctorInfoBO = new DoctorInfoBO();
        doctorInfoBO.setProviderId(123L);
        doctorInfoBO.setName("井泉");
        doctorInfoBO.setMobile("18868859500");
        doctorInfoBO.setIdentityName("医生");
        doctorInfoBO.setIdentityType(10000);
        doctorInfoBO.setRoleName("主任医生");
        doctorInfoBO.setRoleType(10001);

        DoctorInfoBO doctorInfoBO1 = new DoctorInfoBO();
        doctorInfoBO1.setProviderId(456L);
        doctorInfoBO1.setName("井泉甲");
        doctorInfoBO1.setMobile("18868859501");
        doctorInfoBO1.setIdentityName("医生");
        doctorInfoBO1.setIdentityType(10000);
        doctorInfoBO1.setRoleName("副主任医生");
        doctorInfoBO1.setRoleType(10002);

        DoctorInfoBO doctorInfoBO2 = new DoctorInfoBO();
        doctorInfoBO2.setProviderId(789L);
        doctorInfoBO2.setName("井泉乙");
        doctorInfoBO2.setMobile("18868859502");
        doctorInfoBO2.setIdentityName("医生");
        doctorInfoBO2.setIdentityType(10000);
        doctorInfoBO2.setRoleName("主治医生");
        doctorInfoBO2.setRoleType(10003);

        if (StringUtils.equals("井泉", request.getName())) {
            doctorInfoBOList.add(doctorInfoBO);
            doctorInfoBOList.add(doctorInfoBO1);
            doctorInfoBOList.add(doctorInfoBO2);
        } else if (StringUtils.equals("井泉甲", request.getName())) {
            doctorInfoBOList.add(doctorInfoBO1);
        } else if (StringUtils.equals("井泉乙", request.getName())) {
            doctorInfoBOList.add(doctorInfoBO1);
        }

        if (StringUtils.equals("18868859500", request.getMobile())) {
            doctorInfoBOList.add(doctorInfoBO);
        } else if (StringUtils.equals("18868859501", request.getMobile())) {
            doctorInfoBOList.add(doctorInfoBO1);
        } else if (StringUtils.equals("18868859502", request.getMobile())) {
            doctorInfoBOList.add(doctorInfoBO2);
        }

        log.info("queryDoctor param:{}, result:{}", JSON.toJSONString(request), JSON.toJSONString(doctorInfoBOList));
        return doctorInfoBOList;

//        WebClient webClient = buildWebClient();
//
//        MedicalProviderQueryBO queryBO = new MedicalProviderQueryBO();
//        queryBO.setName(request.getName());
//        if (StringUtils.isNotBlank(request.getMobile())) {
//            queryBO.setMobiles(request.getMobile());
//        }
//
//        //定义query参数
//        MultiValueMap<String, String> queryParams = BeanUtil.convertEntityToMultiValueMap(queryBO);
//        //定义url参数
//        Map<String, Object> uriVariables = BeanUtil.convertEntityToMap(queryBO);
//
//        String uri = UriComponentsBuilder.fromHttpUrl(properties.getProviderPageUrl())
//                .queryParams(queryParams)
//                .encode(StandardCharsets.UTF_8) // 可选，默认使用 UTF-8 编码
//                .toUriString();
//
//        Mono<RestResult<PageInfo<MedicalProviderBO>>> responseMono = webClient.get()
//                .uri(uri)
//                .retrieve()
//                .onStatus(HttpStatusCode::is4xxClientError, response -> {
//                    // 处理 4xx 错误
//                    return response.bodyToMono(String.class).flatMap(errorBody -> Mono.error(new RuntimeException("Client Error: " + errorBody)));
//                })
//                .onStatus(HttpStatusCode::is5xxServerError, response -> {
//                    // 处理 5xx 错误
//                    return Mono.error(new RuntimeException("Server Error"));
//                })
//                .bodyToMono(new ParameterizedTypeReference<>() {});
//
//        RestResult<PageInfo<MedicalProviderBO>> restResult = responseMono.block();
//        if (restResult.getCode() != 0) {
//            log.info("Failed to fetch Medical Provider, param:{}, result:{} ", JSON.toJSONString(request), JSON.toJSONString(restResult));
//        }
//
//        PageInfo<MedicalProviderBO> pageInfo = restResult.getData();
//        log.info("listMedicalProvider param:{}, result:{}", JSON.toJSONString(request), JSON.toJSONString(pageInfo));
//        return pageInfo.getList();
    }

    public Flux<List<DoctorInfoBO>> streamQueryDoctor(DoctorQueryRequest request) {
        // 参数校验
        if (request == null) {
            log.warn("Request is null, returning empty list");
            return Flux.just(Collections.emptyList());
        }

        return Mono.fromCallable(() -> queryDoctor(request))
                .doOnError(error -> log.error("Error occurred while processing request: {}", error.getMessage(), error))
                .onErrorResume(error -> {
                    log.warn("Unexpected error occurred, returning empty list: {}", error.getMessage());
                    return Mono.just(Collections.emptyList());
                })
                .flux();
    }

    public String updateDoctor(DoctorUpdateRequest request) {
        log.info("updateDoctor param:{}", JSON.toJSONString(request));
        return "更新操作成功";
    }

    public String insertDoctor(DoctorUpdateRequest request) {
        log.info("insertDoctor param:{}", JSON.toJSONString(request));
        return "新增操作成功";
    }

    public String operateDoctor(DoctorOperateRequest request) {
        log.info("operateDoctor param:{}", JSON.toJSONString(request));
        DoctorOperateType operateType = DoctorOperateType.valueOfCode(request.getOperateType());
        if (operateType == null) {
            log.warn("Invalid operateType: {}, returning false", request.getOperateType());
            return "操作失败，未找到对应的操作类型";
        }
        switch (operateType) {
            case CANCEL_DOCTOR_USER:
                return cancelProviderUser(request);
            case CANCEL_DOCTOR_ACCOUNT:
                return cancelProviderAccount(request);
            case RESET_DOCTOR_INFO:
                return "操作成功";
            default:
                return "操作失败，对应的操作类型未定义操作函数";
        }
    }

    /**
     * @description: 医供用户注销
     * @author: jingquan
     * @date: 2024/12/18 10:45
     * @return: boolean
     **/
    public String cancelProviderUser(DoctorOperateRequest request){
        DoctorCancelReqBO reqBO = new DoctorCancelReqBO();
        reqBO.setProviderId(Long.valueOf(request.getProviderId()));
        reqBO.setIfCheckOrder(1);
        reqBO.setIfSyncSaaS(0);
        reqBO.setIfSyncUser(0);

        String result = "操作成功";
        log.info("cancelProviderUser request:{}, result:{}", JSON.toJSONString(reqBO), result);
        return result;
    }

    /**
     * @description: 医供账号注销
     * @author: jingquan
     * @date: 2024/12/18 10:45
     * @return: boolean
     **/
    public String cancelProviderAccount(DoctorOperateRequest request) {
        DoctorCancelReqBO reqBO = new DoctorCancelReqBO();
        reqBO.setProviderId(Long.valueOf(request.getProviderId()));
        reqBO.setIfCheckOrder(1);
        reqBO.setIfSyncSaaS(1);
        reqBO.setIfSyncUser(1);

        String result = "操作成功";
//        WebClient webClient = buildWebClient();
//        Mono<RestResult<Boolean>> responseMono = webClient.post()
//                .uri(properties.getProviderCancelUrl())
//                .bodyValue(reqBO)
//                .retrieve()
//                .bodyToMono(new ParameterizedTypeReference<>() {});
//
//        RestResult<Boolean> restResult = responseMono.block();
//        if (restResult.getCode() != 0) {
//            log.info("Failed to cancel Medical Provider User, param:{}, result:{} ", JSON.toJSONString(request), JSON.toJSONString(restResult));
//        }
//
//        return restResult.getData();

        log.info("cancelProviderAccount request:{}, result:{}", JSON.toJSONString(reqBO), result);
        return result;
    }
}
