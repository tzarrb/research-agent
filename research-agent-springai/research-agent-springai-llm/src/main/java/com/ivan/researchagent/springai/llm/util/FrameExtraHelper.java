package com.ivan.researchagent.springai.llm.util;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/11 16:24
 **/
@Slf4j
public class FrameExtraHelper {

    //private static final File videoUrl = new File("spring-ai-alibaba-examples/multi-model-example/src/main/resources/multimodel/video.mp4");

    private static final String framePath = "/src/main/resources/multimodel/frame/";

    public static List<String> getVideoPic(String videoUrl) {

        List<String> strList = new ArrayList<>();
        File dir = new File(framePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (
                FFmpegFrameGrabber ff = new FFmpegFrameGrabber(videoUrl);
                Java2DFrameConverter converter = new Java2DFrameConverter()
        ) {
            ff.start();
            ff.setFormat("mp4");

            int length = ff.getLengthInFrames();

            Frame frame;
            for (int i = 1; i < length; i++) {
                frame = ff.grabFrame();
                if (frame.image == null) {
                    continue;
                }
                BufferedImage image = converter.getBufferedImage(frame); ;
                String path = framePath + i + ".png";
                File picFile = new File(path);
                ImageIO.write(image, "png", picFile);
                strList.add(path);
            }
            ff.stop();
        }
        catch (Exception e) {
            log.error("getVideoPic error, url:{}", videoUrl, e);
        }

        return strList;
    }

}
