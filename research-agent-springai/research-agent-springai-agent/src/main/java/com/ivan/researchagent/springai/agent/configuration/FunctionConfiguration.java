package com.ivan.researchagent.springai.agent.configuration;

import com.ivan.researchagent.springai.agent.model.bo.doctor.DoctorInfoBO;
import com.ivan.researchagent.springai.agent.model.func.doctor.DoctorOperateRequest;
import com.ivan.researchagent.springai.agent.model.func.doctor.DoctorQueryRequest;
import com.ivan.researchagent.springai.agent.model.func.doctor.DoctorUpdateRequest;
import com.ivan.researchagent.springai.agent.service.doctor.DoctorService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.function.Function;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/02 16:37
 **/
@Slf4j
@Configuration
public class FunctionConfiguration {

    @Resource
    DoctorService doctorService;

//    @Bean
//    public FunctionCallback queryProviderFunction() {
//        return FunctionCallbackWrapper
//                .builder(new BiFunction<ProviderListRequest, ToolContext, List<MedicalProviderBO>>() {
//                    @Override
//                    public List<MedicalProviderBO> apply(ProviderListRequest request, ToolContext toolContext) {
//                        return medicalProviderService.listMedicalProvider(request);
//                    }
//                })
//                .withName("queryProviderFunction")
//                .withDescription("根据姓名或手机号查询医生信息")
//                .withInputType(ProviderListRequest.class)
//                .build();
//    }

//    @Bean
//    @Description("查询医生信息,")
//    public Function<ProviderListRequest, Flux<List<MedicalProviderBO>>> queryProviderFunction() {
//        return medicalProviderService::streamListMedicalProvider;
//    }

    @Bean
    @Description("查询医生信息")
    public Function<DoctorQueryRequest, List<DoctorInfoBO>> queryDoctorFunction() {
        return doctorService::queryDoctor;
    }

    @Bean
    @Description("管理医生信息")
    public Function<DoctorOperateRequest, String> operateDoctorFunction() {
        return doctorService::operateDoctor;
    }

    @Bean
    @Description("更新医生信息")
    public Function<DoctorUpdateRequest, String> updateDoctorFunction() {
        return doctorService::updateDoctor;
    }

//    @Bean
//    @Description("提供查询和管理医生信息的有关问答")
//    public BiFunction<MedicalAgent.Request, ToolContext, String> medicalAgent() {
//        return new MedicalAgent();
//    }
}
