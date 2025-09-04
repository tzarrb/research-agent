package com.ivan.researchagent.core.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/18/周一
 **/
@Data
@TableName("chat_memory") // MyBatis-Plus 表名映射
public class ChatMemory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String conversationId;
    private String type; // "user", "assistant", "system", "tool"
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDel = 0; // 逻辑删除标记
}
