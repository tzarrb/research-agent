package com.ivan.researchagent.springai.agent.graph.core;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.constant.SaverConstant;
import com.alibaba.cloud.ai.graph.checkpoint.savers.FileSystemSaver;
import com.ivan.researchagent.common.utils.IdUtil;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/9/1/周一
 **/
public class GraphUtil {

    public static CompileConfig getCompileConfig() {
        String outputDir = "data/graph/memory/";
        Path outputPath = Paths.get(outputDir);

        FileSystemSaver fileSystemSaver = new FileSystemSaver(outputPath, new GraphSerializer());
        SaverConfig saverConfig = SaverConfig.builder().register(SaverConstant.FILE, fileSystemSaver).build();
        CompileConfig compileConfig = CompileConfig.builder().saverConfig(saverConfig).build();

        return compileConfig;
    }


    public static RunnableConfig getRunnableConfig(String threadId) {
        if (StringUtils.isBlank(threadId)) {
            threadId = IdUtil.nextId().toString();
        }

        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).build();
        return runnableConfig;
    }
}
