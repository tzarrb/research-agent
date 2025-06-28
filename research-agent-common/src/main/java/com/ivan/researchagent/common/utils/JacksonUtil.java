package com.ivan.researchagent.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/28/周六
 **/
public class JacksonUtil {

    /**
     * 使用泛型方法，把json字符串转换为相应的JavaBean对象。
     * (1)转换为普通JavaBean：readValue(json,Student.class)
     * (2)转换为List,如List<Student>,将第二个参数传递为Student
     * [].class.然后使用Arrays.asList();方法把得到的数组转换为特定类型的List
     *
     * @param jsonStr   json串
     * @param valueType javaClass
     * @return
     */
    public static <T> T readValue(String jsonStr, Class<T> valueType) {
        return JacksonUtil.readValue(jsonStr, valueType, null);
    }

    /**
     * json数组转List
     *
     * @param jsonStr      json串
     * @param valueTypeRef 泛型描述
     * @return
     */
    public static <T> T readValue(String jsonStr, TypeReference<T> valueTypeRef) {
        return JacksonUtil.readValue(jsonStr, null, valueTypeRef);
    }

    private static <T> T readValue(String jsonStr, Class<T> valueType, TypeReference<T> valueTypeRef) {
        try {
            if (valueType != null) {
                return getInstance().readValue(jsonStr, valueType);
            } else if (valueTypeRef != null) {
                return getInstance().readValue(jsonStr, valueTypeRef);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 把JavaBean转换为json字符串
     *
     * @param object 对象
     * @return json串
     */
    public static String toJson(Object object) {
        try {
            return getInstance().writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static class SingleFactory {
        private static final ObjectMapper INSTANCE = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    private static ObjectMapper getInstance() {
        return SingleFactory.INSTANCE;
    }
}
