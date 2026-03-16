package com.cesarzxk.initial.videoStreaming.services;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class VideoConversionService {

    private static final Map<String, Integer> RESOLUTIONS = new LinkedHashMap<>();

    static {
        RESOLUTIONS.put("144", 144);
        RESOLUTIONS.put("240", 240);
        RESOLUTIONS.put("360", 360);
        RESOLUTIONS.put("480", 480);
        RESOLUTIONS.put("720", 720);
        RESOLUTIONS.put("1080", 1080);
    }

    public ConversionResult convertToMultipleQualities(Path inputFile, Path outputDir) throws Exception {
        Files.createDirectories(outputDir);

        String randomId = UUID.randomUUID().toString().replace("-", "");
        Map<String, String> generatedFiles = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : RESOLUTIONS.entrySet()) {
            String label = entry.getKey();
            Integer height = entry.getValue();

            String outputFileName = randomId + "_" + label + ".mp4";
            Path outputFile = outputDir.resolve(outputFileName);

            runFfmpeg(inputFile, outputFile, height);

            generatedFiles.put(label, outputFileName);
        }

        return new ConversionResult(randomId, generatedFiles);
    }

    private void runFfmpeg(Path inputFile, Path outputFile, int targetHeight) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-i", inputFile.toAbsolutePath().toString(),
                "-vf", "scale=-2:" + targetHeight,
                "-c:v", "libx264",
                "-preset", "medium",
                "-crf", "23",
                "-c:a", "aac",
                "-b:a", "128k",
                "-movflags", "+faststart",
                outputFile.toAbsolutePath().toString()
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder logs = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logs.append(line).append(System.lineSeparator());
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Error ao converter vídeo para " + targetHeight + "p.\n" + logs);
        }
    }

    public record ConversionResult(String videoId, Map<String, String> files) {}
}