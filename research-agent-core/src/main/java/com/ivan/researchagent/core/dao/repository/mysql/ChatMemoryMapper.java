package com.ivan.researchagent.core.dao.repository.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ivan.researchagent.common.dynamic.DS;
import com.ivan.researchagent.core.dao.entity.ChatMemory;
import org.apache.ibatis.annotations.Mapper;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/18/周一
 **/

@DS("postgresql")
@Mapper
public interface ChatMemoryMapper extends BaseMapper<ChatMemory> {
    // 继承 BaseMapper 即拥有 CRUD 方法
}
