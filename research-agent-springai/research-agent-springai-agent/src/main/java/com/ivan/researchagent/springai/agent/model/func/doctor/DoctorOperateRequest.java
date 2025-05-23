package com.ivan.researchagent.springai.agent.model.func.doctor;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/18 10:47
 **/
@Data
@JsonClassDescription("医生管理操作")
public class DoctorOperateRequest {

    @JsonProperty(required = true, value = "providerId")
    @JsonPropertyDescription("医生用户编号, 比如12344556677777")
    String providerId;

    @JsonProperty(required = true, value = "operateType")
    @JsonPropertyDescription("医生注销操作类型, 比如cancelDoctorUser, cancelDoctorAccount等")
    String operateType;
}
