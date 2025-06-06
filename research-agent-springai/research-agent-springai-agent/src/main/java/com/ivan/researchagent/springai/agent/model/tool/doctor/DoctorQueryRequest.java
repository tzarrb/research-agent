package com.ivan.researchagent.springai.agent.model.tool.doctor;

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
 * @since: 2024/12/23 17:42
 **/
@Data
@JsonClassDescription("根据参数查询医供用户信息")
public class DoctorQueryRequest {
    @JsonProperty(required = false, value = "name")
    @JsonPropertyDescription("姓名, 比如井泉")
    String name;

    @JsonProperty(required = false, value = "mobile")
    @JsonPropertyDescription("手机号, 比如18868868998")
    String mobile;
}
