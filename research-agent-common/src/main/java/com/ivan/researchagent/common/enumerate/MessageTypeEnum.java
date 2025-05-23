package com.ivan.researchagent.common.enumerate;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/11 15:47
 **/
public enum MessageTypeEnum {

    /**
     * text format
     */
    TEXT,

    /**
     * image format
     */
    IMAGE,

    /**
     * video format
     */
    VIDEO;

    public static boolean isMedia(String type) {
        return IMAGE.name().equals(type) || VIDEO.name().equals(type);
    }
}
