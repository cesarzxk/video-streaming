package com.cesarzxk.initial.videoStreaming.services;

import com.cesarzxk.initial.videoStreaming.domain.Quality;
import com.cesarzxk.initial.videoStreaming.dto.UploadResponseDTO;
import com.cesarzxk.initial.videoStreaming.dto.VideoRequestDTO;
import com.cesarzxk.initial.videoStreaming.services.VideoConversionService.ConversionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class StreamService {
    private final VideoConversionService videoConversionService;
    private final StorageService storageService;
    private final Path tempDir = Paths.get("videos", "temp").toAbsolutePath().normalize();
    public final Path encodedDir = Paths.get("videos", "encoded").toAbsolutePath().normalize();
    private final  VideoService videoService;
    private List<Quality> qualities;

    public StreamService(VideoConversionService videoConversionService, StorageService storageService, VideoService videoService, List<Quality> qualities) {
        this.videoConversionService = videoConversionService;
        this.storageService = storageService;
        this.videoService = videoService;
        this.qualities = qualities;
    }

    public ResponseEntity<Resource> getStreamVideo(String name, String rangeHeader) throws Exception {
        long totalSize = storageService.getObjectSize(name);
        long rangeStart = 0;
        long rangeEnd = totalSize - 1;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String rangeValue = rangeHeader.substring("bytes=".length()).trim();
            String[] parts = rangeValue.split("-", 2);
            try {
                if (parts.length == 2) {
                    if (!parts[0].isEmpty()) {
                        rangeStart = Long.parseLong(parts[0]);
                    }
                    if (!parts[1].isEmpty()) {
                        rangeEnd = Long.parseLong(parts[1]);
                    }
                } else if (parts.length == 1 && !parts[0].isEmpty()) {
                    rangeStart = Long.parseLong(parts[0]);
                }
            } catch (NumberFormatException ex) {
                return ResponseEntity.status(416).header(HttpHeaders.CONTENT_RANGE, "bytes */" + totalSize).build();
            }
        }

        if (rangeStart < 0) rangeStart = 0;
        if (rangeEnd >= totalSize) rangeEnd = totalSize - 1;
        if (rangeStart > rangeEnd) {
            return ResponseEntity.status(416).header(HttpHeaders.CONTENT_RANGE, "bytes */" + totalSize).build();
        }

        long contentLength = rangeEnd - rangeStart + 1;

        InputStream is = storageService.getVideo(name, rangeStart, contentLength);
        InputStreamResource resource = new InputStreamResource(is);

        String contentType = "video/mp4";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + name + "\"");
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        headers.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Accept-Ranges, Content-Range, Content-Length, Content-Type");
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.set(HttpHeaders.CONTENT_TYPE, contentType);
        headers.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));

        if (rangeHeader != null) {
            headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + rangeStart + "-" + rangeEnd + "/" + totalSize);
            return ResponseEntity.status(206).headers(headers).body(resource);
        }

        return ResponseEntity.ok().headers(headers).body(resource);
    }

    public UploadResponseDTO convertVideos(MultipartFile file) throws Exception {
        Path tempFile = null;

        try {
            Files.createDirectories(tempDir);
            Files.createDirectories(encodedDir);

            String tempName = UUID.randomUUID().toString().replace("-", "") + "_original";
            String originalName = file.getOriginalFilename();
            String extension = getExtension(originalName);

            tempFile = tempDir.resolve(tempName + extension);
            file.transferTo(tempFile);

            ConversionResult result = videoConversionService.convertToMultipleQualities(tempFile, encodedDir);

            Map<String, String> storedFiles = new java.util.LinkedHashMap<>();
            for (Map.Entry<String, String> e : result.files().entrySet()) {
                String label = e.getKey();
                String fileName = e.getValue();
                Path filePath = encodedDir.resolve(fileName).toAbsolutePath().normalize();

                String objectName = storageService.setVideo(filePath);
                storedFiles.put(label, objectName);

                qualities.add(Quality.Q144);

                try {
                    Files.deleteIfExists(filePath);
                } catch (Exception ex) {
                    System.err.println("Falha ao apagar arquivo convertido local: " + filePath + " -> " + ex.getMessage());
                }
            }

            try {


                VideoRequestDTO newVideoEntity = VideoRequestDTO.builder()
                        .title(tempName)
                        .url(tempName.replace("_original", "") + ".mp4")
                        .description(tempName)
                        .thumbnailUrl(tempName)
                        .uploaderName(tempName)
                        .duration(file.getSize())
                        .qualities(qualities)
                        .build();

                videoService.setVideo(newVideoEntity);

            } catch (Exception ex) {
                log.error("Failed to save video metadata for {}: {}", tempName, ex.getMessage());
                throw new IllegalStateException("Vídeo carregado, mas falha ao salvar metadados: " + ex.getMessage(), ex);
            }

            return new UploadResponseDTO("Vídeo convertido com sucesso", storedFiles, storedFiles);
        } finally {
            try {
                if (tempFile != null) {
                    Files.deleteIfExists(tempFile);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ".mp4";
        }

        return fileName.substring(fileName.lastIndexOf("."));
    }
}