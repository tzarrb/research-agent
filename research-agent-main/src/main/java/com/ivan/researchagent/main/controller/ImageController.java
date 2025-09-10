package com.ivan.researchagent.main.controller;

import com.ivan.researchagent.springai.llm.model.image.ImageRequest;
import com.ivan.researchagent.springai.llm.service.ImageService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/09 17:47
 **/
@RestController
@RequestMapping("/image")
@CrossOrigin(origins = "*")
public class ImageController {

    @Resource
    private ImageService imageService;

    @PostMapping("")
    public void image(@RequestBody ImageRequest input, HttpServletResponse response) {
        String imageUrl = imageService.genImage(input);

        try {
            URL url = new URL(imageUrl);
            InputStream in = url.openStream();

            response.setHeader("Content-Type", MediaType.IMAGE_PNG_VALUE);
            response.getOutputStream().write(in.readAllBytes());
            response.getOutputStream().flush();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/url")
    public String imageUrl(@RequestBody ImageRequest input) {
        String imageUrl = imageService.genImage(input);
        return imageUrl;
    }
}
