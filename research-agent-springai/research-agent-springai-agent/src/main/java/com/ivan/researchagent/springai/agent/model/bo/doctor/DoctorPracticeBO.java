package com.ivan.researchagent.springai.agent.model.bo.doctor;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * 医供执业点信息
 *
 * @author: ivan
 * @since: 2020/9/8 19:41
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorPracticeBO implements Serializable {

    private static final long serialVersionUID = -407958405546400414L;

    /**
     * 执业点id
     */
    private Long practiceId;

    /**
     * 医供者id
     */
    private Long providerId;

    /**
     * 执业科室关联id
     */
    private Long practiceDeptId;

    /**
     * 微脉号
     */
    private Long registerId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 身份证件号
     */
    private String idCard;
    /**
     * 身份证件类型
     */
    private String idCardType;
    /**
     * 证件类型中文描述
     */
    private String idCardTypeDesc;
    /**
     * 性别
     */
    private Byte sex;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 头像链接地址
     */
    private String avatarUrl;

    /**
     * 职业
     */
    private Integer profession;

    /**
     * 职业名称
     */
    private String professionName;

    /**
     * 职称
     */
    private Integer professionTitle;

    /**
     * 职称名称
     */
    private String professionTitleName;

    /**
     * 简介
     */
    private String summary;

    /**
     * 擅长
     */
    private String skill;

    /**
     * 1.人工添加，2.自主注册，3.后台添加，4.HIS导入，5.互联网医院导入
     */
    private Byte source;

    /**
     * 执业点类型，0.未知，1微脉机构执业点，2HIS导入执业点，3.SaaS入驻执业点
     */
    private Byte type;

    /**
     * 机构id
     */
    private Long institutionId;

    /**
     * 机构名称
     */
    private String institutionName;

    /**
     * 医疗机构等级 10-未定级 11-一级丙等 12-一级乙等 13-一级甲等 21-二级丙等 22-二级乙等 23-二级甲等 31-三级丙等 32-三级乙等 33-三级甲等
     */
    private Integer hospitalLevel;

    /**
     * 医院等级描述
     */
    private String hospitalLevelDesc;

    /**
     * 科室id
     */
    private Long departmentId;

    /**
     * 科室名称
     */
    private String departmentName;

    /**
     * 科室院内编号
     */
    private String departmentHospitalCode;

    /**
     * 职工id
     */
    private Long employeeId;

    /**
     * 区域id
     */
    private Long areaId;

    /**
     * 主执业点id
     */
    private Long mainPracticeId;

    /**
     * 线上执业机构id，医生线上业务的所属机构
     */
    private Long practiceInstitutionId;

    /**
     * 线上执业机构名称，医生线上业务的所属机构
     */
    private String practiceInstitutionName;

    /**
     * 线上执业科室id，医生线上业务的所属科室
     */
    private Long practiceDepartmentId;

    /**
     * 线上执业科室名称，医生线上业务的所属科室
     */
    private String practiceDepartmentName;

    /**
     * 医生端注册执业点id
     */
    private Long registerPracticeId;

    /**
     * 执业点工作状态，0.禁用、1.在职、2离职、3.停职、4.退休
     */
    private Byte status;

    /**
     * 执业点认证状态，0.待认证，1.待审核，2.通过，3.不通过
     */
    private Byte authStatus;

    /**
     * 执业点认证时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date authTime;

    /**
     * 执业点状态，1.正常，2.封禁
     */
    private Byte practiceStatus;

    /**
     * 执业资格证书
     */
    private String practiceCertificate;

    /**
     * 工号,工牌上的号码
     */
    private String employeeJobNum;

    /**
     * 院内医生编号，医生HIS导入
     */
    private String employeeHospitalCode;

    /**
     * 医供者状态，1.正常、2.封禁
     */
    private Byte providerStatus;

    /**
     * 医供者认证状态，0.待认证，1.待审核，2.通过，3.不通过
     */
    private Byte providerAuthStatus;

    /**
     * 医供者申请时间
     */
    private Date providerApplyTime;

    /**
     * 医供者审核时间
     */
    private Date providerAuthTime;

    /**
     * 医供者认证资料
     */
    private String providerAuthUrl;

    /**
     * 多执业点信息
     */
    private List<DoctorPracticeBO> medicalPractices;

    /**
     * 职业范围
     */
//    private List<PracticeScopeBO> practiceScopes;

    /**
     * 职业范围编码
     */
    private List<String> practiceScopeCodes;

    /**
     * 执业点标签
     */
    private List<String> practiceTags;

    /**
     * 业务功能
     */
//    private List<PracticeFunctionBO> functionTypes;

    /**
     * 服务标签，对功能类型的产品定义
     */
//    private List<PracticeServiceTagBO> serviceTags;

    /**
     * 咨询人数
     */
    private String adviceCount;

    /**
     * 挂号量
     */
    private String registrationCount;

    /**
     * 关注数
     */
    private String fansCount;

    /**
     * 好评率
     */
    private String favorableRate;

    /**
     * 评分
     */
    private String score;

    /**
     * 执业点信息是否可被HIS数据覆盖,0.否，1.是
     */
    private Byte isHisCover;

    /**
     * 是否标准执业点数据，如特殊名称，特殊医生，0.否，1.是
     */
    private Byte isStandard;

    /**
     * 是否隐藏，0隐藏 1显示
     */
    private Byte isHide;

    /**
     * 隐藏原因
     */
    private String hideReason;

    /**
     * 隐藏操作人
     */
    private String hideOperator;
    /**
     * 是否主执业点,0.否，1.是
     */
    private Byte ifMain;

    /**
     * 修改时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DoctorPracticeBO that = (DoctorPracticeBO) o;

        if (!practiceId.equals(that.practiceId)) return false;
        return practiceDeptId != null ? practiceDeptId.equals(that.practiceDeptId) : that.practiceDeptId == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + practiceId.hashCode();
        result = 31 * result + (practiceDeptId != null ? practiceDeptId.hashCode() : 0);
        return result;
    }
}
