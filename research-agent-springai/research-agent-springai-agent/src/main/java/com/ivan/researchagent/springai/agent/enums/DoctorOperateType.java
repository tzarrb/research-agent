package com.ivan.researchagent.springai.agent.enums;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/1/8/周三
 **/
public enum DoctorOperateType {
    RESET_DOCTOR_INFO("resetDoctorInfo","重置医生信息"),
    CANCEL_DOCTOR_USER("cancelDoctorUser","注销医生用户信息"),
    CANCEL_DOCTOR_ACCOUNT("cancelDoctorAccount","注销医生用户信息"),
    ;

    private String code;
    private String desc;

    DoctorOperateType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
   }

   public static DoctorOperateType valueOfCode(String code) {
       for (DoctorOperateType type : DoctorOperateType.values()) {
           if (type.getCode().equals(code)) {
               return type;
           }
       }

       return null;
   }
}
