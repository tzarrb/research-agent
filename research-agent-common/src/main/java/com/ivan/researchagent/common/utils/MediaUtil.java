package com.ivan.researchagent.common.utils;

import com.ivan.researchagent.common.enumerate.MessageTypeEnum;
import jdk.jfr.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/9/8/周一
 **/
@Slf4j
public class MediaUtil {

    private static final String framePath = "data/temp/frame/";

    /**
     * 获取视频的帧图片
     * @param videoUrl 视频URL
     * @param frameCount 要提取的帧数
     * @return 帧图片文件列表
     */
    public static List<String> extractFrames(String videoUrl, int frameCount) throws IOException {
        List<String> pathList = new ArrayList<>();
        FFmpegFrameGrabber ff = new FFmpegFrameGrabber(videoUrl);
        List<File> frames = extractFrames(ff, frameCount);
        frames.forEach(file -> pathList.add(file.getPath()));
        return pathList;
    }

    /**
     * 从视频中提取指定数量的帧
     * @param videoFile 视频文件
     * @param frameCount 要提取的帧数
     * @return 提取的帧图片文件列表
     */
    public static List<File> extractFrames(File videoFile, int frameCount) throws IOException {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFile);
        return extractFrames(grabber, frameCount);
    }

    /**
     * 从视频中提取指定数量的帧
     * @param grabber 视频抓取器
     * @param frameCount 要提取的帧数
     * @return 提取的帧图片文件列表
     */
    public static List<File> extractFrames(FFmpegFrameGrabber grabber, int frameCount) throws IOException {
        List<File> frames = new ArrayList<>();

        try {
            grabber.start();
            //grabber.setFormat("mp4");

            // 创建临时目录（如果不存在）
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), framePath);
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }

            int totalFrames = grabber.getLengthInFrames();
            int step = Math.max(1, totalFrames / frameCount); // 计算帧间隔

            Java2DFrameConverter converter = new Java2DFrameConverter();
            for (int i = 0; i < frameCount; i++) {
                int frameNumber = i * step; // 计算当前帧位置
                grabber.setFrameNumber(frameNumber);
                Frame frame = grabber.grabImage();
                if (frame == null || frame.image == null) {
                    continue;
                }

                BufferedImage image = converter.convert(frame);

                // 保存帧为PNG图片
                File outputFile = tempDir.resolve(UUID.randomUUID() + ".png").toFile();
                ImageIO.write(image, "png", outputFile);
                frames.add(outputFile);
            }
        } finally {
            grabber.stop();
        }

        return frames;
    }

    /**
     * 通过MultipartFile判断内容类型
     * @param file 文件
     * @return 内容类型枚举
     */
    public static MessageTypeEnum getContentType(MultipartFile file) {
        if (file == null) return MessageTypeEnum.TEXT;

        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return MessageTypeEnum.IMAGE;
            } else if (contentType.startsWith("video/")) {
                return MessageTypeEnum.VIDEO;
            } else if (contentType.startsWith("audio/")) {
                return MessageTypeEnum.AUDIO;
            }
        }

        return MessageTypeEnum.IMAGE;
    }

    /**
     * 通过URL判断内容类型
     * @param urlString URL链接
     * @return 内容类型枚举
     */
    public static MessageTypeEnum getContentType(String urlString) {
        if (StringUtils.isBlank(urlString)) return MessageTypeEnum.TEXT;

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");

            // 优先通过Content-Type头判断
            String contentType = connection.getContentType();
            if (contentType != null) {
                if (contentType.startsWith("image/")) {
                    return MessageTypeEnum.IMAGE;
                } else if (contentType.startsWith("video/")) {
                    return MessageTypeEnum.VIDEO;
                } else if (contentType.startsWith("audio/")) {
                    return MessageTypeEnum.AUDIO;
                }
            }

            // 回退到扩展名判断
            String lowerCaseUrl = urlString.toLowerCase();
            if (hasImageExtension(lowerCaseUrl)) return MessageTypeEnum.IMAGE;
            if (hasVideoExtension(lowerCaseUrl)) return MessageTypeEnum.VIDEO;
            if (hasAudioExtension(lowerCaseUrl)) return MessageTypeEnum.AUDIO;

        } catch (Exception e) {
            // 日志记录可替换为实际日志框架
            System.err.println("Error determining content type: " + e.getMessage());
        }
        return MessageTypeEnum.IMAGE;
    }

    // 图片扩展名检查
    private static boolean hasImageExtension(String url) {
        return url.endsWith(".jpg") || url.endsWith(".jpeg") ||
                url.endsWith(".png") || url.endsWith(".gif");
    }

    // 视频扩展名检查
    private static boolean hasVideoExtension(String url) {
        return url.endsWith(".mp4") || url.endsWith(".avi") ||
                url.endsWith(".mov") || url.endsWith(".mkv");
    }

    // 音频扩展名检查
    private static boolean hasAudioExtension(String url) {
        return url.endsWith(".mp3") || url.endsWith(".wav") ||
                url.endsWith(".ogg");
    }


    public static void main(String[] args) {
        String url = "https://avatars.githubusercontent.com/u/7997078?v=4";
        MessageTypeEnum contentType = getContentType(url);
        System.out.println("Content Type: " + contentType);
    }
}

