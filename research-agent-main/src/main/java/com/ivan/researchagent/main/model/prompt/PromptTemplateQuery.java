package com.ivan.researchagent.main.model.prompt;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/11/29 16:39
 **/
@Data
public class PromptTemplateQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    private String templateName;

    private String templateContent;

    private Map<String, Object> templateParam;
}
