package com.ivan.researchagent.springai.agent.model.func.doctor;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/1/9/周四
 **/
@Data
public class DoctorUpdateRequest {

    /**
     * 医生用户编号
     */
    @JsonPropertyDescription("医生用户编号，身份包括医生、护士、医助、个管师等")
    private Long providerId;

    /**
     * 姓名
     */
    @JsonPropertyDescription("姓名")
    private String name;

    /**
     * 手机号
     */
    @JsonPropertyDescription("手机号")
    private String mobile;

    /**
     * 身证份号
     */
    @JsonPropertyDescription("身证份号")
    private String idCard;

    /**
     * 身份类型，10000.医生，20000.护士，50000.创新医助
     */
    @JsonPropertyDescription("身份类型，10000.医生，20000.护士，50000.创新医助")
    private Integer identityType;

    /**
     * 角色类型
     */
    @JsonPropertyDescription("角色类型")
    private Integer roleType;

}
