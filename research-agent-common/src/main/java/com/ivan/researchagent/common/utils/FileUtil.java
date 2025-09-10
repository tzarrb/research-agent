package com.ivan.researchagent.common.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.BindException;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/9/9/周二
 **/
public class FileUtil {


    /**
     * save file to tmp folder
     */
    public static String saveFile(MultipartFile file, String path) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new BindException("File is null or empty");
        }

        // 将文件保存到文件夹下
        String fileName = file.getOriginalFilename();
        String filePath = path + fileName;
        file.transferTo(new java.io.File(filePath));
        return filePath;
    }
}
