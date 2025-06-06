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
 * @author: ivan
 * @since: 2025/1/4 21:42
 */
@Data
@JsonClassDescription("医生查询或操作处理参数")
public class DoctorAgentRequest {

    @JsonProperty(required = false, value = "input")
    @JsonPropertyDescription("姓名,手机号或用户编号, 比如井泉、18868858588、123456")
    String input;
}
