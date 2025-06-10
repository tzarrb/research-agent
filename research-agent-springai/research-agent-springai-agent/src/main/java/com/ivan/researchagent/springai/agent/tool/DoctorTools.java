package com.ivan.researchagent.springai.agent.tool;

import com.ivan.researchagent.springai.agent.anno.ToolWarpper;
import com.ivan.researchagent.springai.agent.model.bo.doctor.DoctorInfoBO;
import com.ivan.researchagent.springai.agent.model.tool.doctor.DoctorOperateRequest;
import com.ivan.researchagent.springai.agent.model.tool.doctor.DoctorQueryRequest;
import com.ivan.researchagent.springai.agent.model.tool.doctor.DoctorUpdateRequest;
import com.ivan.researchagent.springai.agent.service.doctor.DoctorService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/4/7/周一
 **/
@Slf4j
@ToolWarpper
public class DoctorTools {

    @Resource
    DoctorService doctorService;

    @Tool(description = "查询医生信息", returnDirect = true)
    List<DoctorInfoBO> queryDoctor(DoctorQueryRequest request, ToolContext toolContext) {
        return doctorService.queryDoctor(request);
    }

    @Tool(description = "管理医生信息，包括注销用户信息，注销账户信息，重置", returnDirect = true)
    String operateDoctor(DoctorOperateRequest request, ToolContext toolContext) {
        return doctorService.operateDoctor(request);
    }

    @Tool(description = "更新医生信息", returnDirect = true)
    String updateDoctor(DoctorUpdateRequest request, ToolContext toolContext) {
        return doctorService.updateDoctor(request);
    }

    @Tool(description = "新增医生信息", returnDirect = true)
    String insertDoctor(DoctorUpdateRequest request, ToolContext toolContext) {
        return doctorService.insertDoctor(request);
    }

}
