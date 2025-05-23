package com.ivan.researchagent.springai.agent.model.bo.doctor;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/02 16:46
 **/
@Data
@JsonClassDescription("医供用户信息")
public class DoctorInfoBO implements Serializable {

    /**
     * 医供者id
     */
    @JsonPropertyDescription("医供者用户id，医供者包括医生、护士、医助、个管师等")
    private Long providerId;

    /**
     * 微脉号
     */
    @JsonPropertyDescription("微脉号，由微脉平台分配")
    private Long registerId;

    /**
     * 姓名
     */
    @JsonPropertyDescription("姓名")
    private String name;

    /**
     * 真实姓名
     */
    @JsonPropertyDescription("真实姓名")
    private String realName;

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
     * 身份证件类型
     */
    @JsonPropertyDescription("身证份类型，1.身份证，2.护照，3.军官证，4.港澳通行证，5.台胞证，6.外国人永久居留证，7.外国人临时居留证，8.外国人永久居留身份证，9.)其他")
    private String idCardType;

    /**
     * 性别
     */
    @JsonPropertyDescription("性别，1.男，2.女")
    private Byte sex;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 头像链接地址
     */
    @JsonPropertyDescription("头像链接地址")
    private String avatarUrl;

    /**
     * 生日
     */
    @JsonPropertyDescription("生日")
    private Date birthDay;

    /**
     * 身份类型，10000.医生，20000.护士，50000.创新医助
     */
    @JsonPropertyDescription("身份类型，10000.医生，20000.护士，50000.创新医助")
    private Integer identityType;

    /**
     * 身份类型名称
     */
    @JsonPropertyDescription("身份类型名称")
    private String identityName;

    /**
     * 角色类型
     */
    @JsonPropertyDescription("角色类型")
    private Integer roleType;

    /**
     * 角色名称
     */
    @JsonPropertyDescription("角色名称")
    private String roleName;

    /**
     * 当前主区域id
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 当前主机构id
     */
    private Long institutionId;

    /**
     * 当前主机构名称
     */
    private String institutionName;

    /**
     * 主职业点科室id
     */
    private Long departmentId;

    /**
     * 主职业点科室名称
     */
    private String departmentName;

    /**
     * 1.系统添加，2.自主注册，4.HIS导入，5.互联网医院导入
     */
    private Byte source;

    /**
     * 医生身份状态，0.禁用、1.正常、2.封号
     */
    @JsonPropertyDescription("医生身份状态，0.禁用、1.正常、2.封号")
    private Byte status;

    /**
     * 医生身份认证状态，0.待认证，1.待审核，2.通过，3.不通过
     */
    @JsonPropertyDescription("医生身份认证状态，0.待认证，1.待审核，2.通过，3.不通过")
    private Byte authStatus;

    /**
     * 认证时间
     */
    @JsonPropertyDescription("认证时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime authTime;

    /**
     * 简介
     */
    @JsonPropertyDescription("简介")
    private String summary;

    /**
     * 擅长
     */
    @JsonPropertyDescription("擅长")
    private String skill;

}
