package com.cesarzxk.initial.videoStreaming.services;

import com.cesarzxk.initial.videoStreaming.dto.VideoRequestDTO;
import io.minio.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class StorageService {
    private static final Logger log = LoggerFactory.getLogger(StorageService.class);
    @Value("${minio.bucket:streaming-app}")
    private String BUCKET_NAME;
    private final VideoService videoService;
    private final MinioClient minioClient;

    private MinioClient getClient() {
        if (this.minioClient == null) {
            throw new IllegalStateException("MinIO client is not initialized");
        }
        return this.minioClient;
    }

    public StorageService(MinioClient minioClient, VideoService videoService) {
        this.minioClient = minioClient;
        this.videoService = videoService;
        log.info("StorageService initialized");
    }

    private void ensureBucketExists() throws Exception {
        boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(BUCKET_NAME)
                        .build()
        );

        if (!bucketExists) {
            try {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
                log.info("Bucket {} created", BUCKET_NAME);
            } catch (Exception ex) {
                log.error("Failed to create bucket {}: {}", BUCKET_NAME, ex.getMessage());
                throw new IllegalStateException("Bucket " + BUCKET_NAME + " não existe e não pôde ser criado: " + ex.getMessage(), ex);
            }
        }
    }

    public long getObjectSize(String objectName) throws Exception {
        ensureBucketExists();
        io.minio.StatObjectResponse stat = minioClient.statObject(
                io.minio.StatObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(objectName)
                        .build()
        );
        return stat.size();
    }

    public static void deleteFile(Path pathFile) throws Exception {
        Files.deleteIfExists(pathFile);
    }


    public InputStream getVideo(String objectName, long offset, Long length) throws Exception {
        ensureBucketExists();

        GetObjectArgs.Builder builder = GetObjectArgs.builder()
                .bucket(BUCKET_NAME)
                .object(objectName)
                .offset(offset);

        if (length != null) {
            builder.length(length);
        }

        return minioClient.getObject(builder.build());
    }


    public String setVideo(Path path) throws Exception {
        ensureBucketExists();

        if (path == null || !Files.exists(path)) {
            throw new IllegalArgumentException("Arquivo inválido ou inexistente: " + path);
        }

        String originalName = path.getFileName().toString();
        String objectName = UUID.randomUUID() + "_" + originalName;
        long size = Files.size(path);

        try (InputStream inputStream = Files.newInputStream(path)) {
            getClient().putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(Files.probeContentType(path))
                            .build()
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            deleteFile(path);
        }

        return objectName;
    }
}