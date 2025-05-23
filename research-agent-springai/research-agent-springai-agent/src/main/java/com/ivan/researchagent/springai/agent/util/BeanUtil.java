package com.ivan.researchagent.springai.agent.util;

import org.springframework.beans.BeanUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/16 17:03
 **/
public class BeanUtil {

    public static Map<String, Object> convertEntityToMap(Object source) {
        Map<String, Object> target = new HashMap<>();
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(source.getClass());
        for (PropertyDescriptor descriptor : descriptors) {
            if (descriptor.getName().equals("class")) {
                continue;
            }

            String propertyName = descriptor.getName();
            Method readMethod = descriptor.getReadMethod();

            if (readMethod != null) {
                try {
                    Object value = readMethod.invoke(source);
                    if (value != null) {
                        target.put(propertyName, value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Handle exception if needed
                }
            }
        }
        return target;
    }
    public static MultiValueMap<String, String> convertEntityToMultiValueMap(Object source) {
        MultiValueMap<String, String> target = new LinkedMultiValueMap<>();
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(source.getClass());
        for (PropertyDescriptor descriptor : descriptors) {
            if (descriptor.getName().equals("class")) {
                continue;
            }

//            Object value = BeanUtils.getPropertyValue(source, descriptor.getName());
//            if (value != null) {
//                target.add(descriptor.getName(), value.toString());
//            }

            String propertyName = descriptor.getName();
            Method readMethod = descriptor.getReadMethod();

            if (readMethod != null) {
                try {
                    Object value = readMethod.invoke(source);
                    if (value != null) {
                        // Convert value to String, handle different types if necessary
                        String stringValue = value.toString();
                        // 手动编码每个值
                        //String encodedValue = URLEncoder.encode(stringValue, StandardCharsets.UTF_8);
                        target.add(propertyName, stringValue);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Handle exception if needed
                }
            }
        }
        return target;
    }
}
