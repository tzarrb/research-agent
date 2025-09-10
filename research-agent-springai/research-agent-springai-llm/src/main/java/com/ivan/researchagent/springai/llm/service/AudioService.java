package com.ivan.researchagent.springai.llm.service;

import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioTranscriptionOptions;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.SpeechSynthesisModel;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.SpeechSynthesisPrompt;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.SpeechSynthesisResponse;
import com.alibaba.cloud.ai.dashscope.audio.transcription.AudioTranscriptionModel;
import com.ivan.researchagent.common.exception.BizException;
import com.ivan.researchagent.common.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.core.io.FileUrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/9/9/周二
 **/
@Slf4j
@Service
public class AudioService {

    private final AudioTranscriptionModel transcriptionModel;

    private final SpeechSynthesisModel speechSynthesisModel;

    private final String DEFAULT_MODEL = "paraformer-realtime-v2";

    private static final String DEFAULT_MODEL_1 = "sensevoice-v1";

    public AudioService(
            AudioTranscriptionModel transcriptionModel,
            SpeechSynthesisModel speechSynthesisModel
    ) {

        this.transcriptionModel = transcriptionModel;
        this.speechSynthesisModel = speechSynthesisModel;
    }

    /**
     * Convert text to speech
     */
    public byte[] text2audio(String prompt) {

        Flux<SpeechSynthesisResponse> response = speechSynthesisModel.stream(
                new SpeechSynthesisPrompt(prompt)
        );

        CountDownLatch latch = new CountDownLatch(1);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            response.doFinally(
                    signal -> latch.countDown()
            ).subscribe(synthesisResponse -> {

                ByteBuffer byteBuffer = synthesisResponse.getResult().getOutput().getAudio();
                byte[] bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);

                try {
                    outputStream.write(bytes);
                }
                catch (IOException e) {
                    throw new BizException("Error writing to output stream " + e.getMessage());
                }
            });

            latch.await();
        }
        catch (InterruptedException e) {
            throw new BizException("Operation interrupted. " + e.getMessage());
        }

        return outputStream.toByteArray();
    }

    /**
     * Convert speech to text
     * Emm~, has error.
     */
    public String audio2text(MultipartFile file) throws IOException {
        String filePath = FileUtil.saveFile(file, "/data/temp/audio/");

        return transcriptionModel.call(
                new AudioTranscriptionPrompt(
                        new FileUrlResource(filePath),
                        DashScopeAudioTranscriptionOptions.builder()
                                .withModel(DEFAULT_MODEL_1)
                                .build()
                )
        ).getResult().getOutput();
    }

}
