package com.ivan.researchagent.springai.agent.model.bo;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/16 16:51
 **/
@Data
public class PageInfo<T> implements Serializable {
    private static final long serialVersionUID = 7686114838736172544L;
    @JsonPropertyDescription("当前第几页")
    private Integer pageNum = 1;
    @JsonPropertyDescription("每页数量")
    private Integer pageSize = 10;
    @JsonPropertyDescription("当前页的数量")
    private Integer size;
    @JsonPropertyDescription("总记录数")
    private Long total;
    @JsonPropertyDescription("总页数")
    private Integer pages;
    @JsonPropertyDescription("结果集合")
    private List<T> list;
    private Map<String, Object> ext = new HashMap();
}
