package com.ivan.researchagent.common.utils;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/8/12/周二
 **/
public class IdUtil {
    private static long sequence = 0L;
    private static long lastTimestamp = -1L;

    public static Long nextId() {
        long id = snowflakeGenerator(1, 1);
        return id;
    }

    private static Long snowflakeGenerator(long datacenterId, long workerId) {
        if (datacenterId > 31 || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId must be between 0 and 31");
        }
        if (workerId > 31 || workerId < 0) {
            throw new IllegalArgumentException("workerId must be between 0 and 31");
        }

        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，无法生成ID");
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & 4095;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        return ((timestamp - 1288834974657L) << 22) |
                (datacenterId << 17) |
                (workerId << 12) |
                sequence;
    }

    private static long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
