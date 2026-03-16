package com.cesarzxk.initial.videoStreaming.services;

import io.minio.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class StorageService {
    private static final Logger log = LoggerFactory.getLogger(StorageService.class);
    @Value("${minio.bucket:teste}")
    private String BUCKET_NAME;

    private final MinioClient minioClient;

    public StorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
        log.info("StorageService initialized");
    }

    public static void deleteFile(Path pathFile) throws Exception {
        Files.deleteIfExists(pathFile);
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

    public String setVideo(MultipartFile file) throws Exception {
        ensureBucketExists();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo inválido ou vazio");
        }

        String originalName = file.getOriginalFilename();
        String objectName = UUID.randomUUID() + "_" + (originalName != null ? originalName : "video");

        try (InputStream inputStream = file.getInputStream()) {
            MinioClient client = getClient();
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }
        return objectName;
    }

    public String setVideo(Path filePath) throws Exception {
        ensureBucketExists();
        if (filePath == null || !Files.exists(filePath) || Files.size(filePath) == 0) {
            throw new IllegalArgumentException("Arquivo inválido ou inexistente: " + filePath);
        }

        String originalName = filePath.getFileName().toString();

            long size = Files.size(filePath);
            String contentType = Files.probeContentType(filePath);

            try (InputStream inputStream = Files.newInputStream(filePath)) {
                MinioClient client = getClient();
                client.putObject(
                        PutObjectArgs.builder()
                                .bucket(BUCKET_NAME)
                                .object(originalName)
                                .stream(inputStream, size, -1)
                                .contentType(contentType)
                                .build()
                );
            }finally {
                StorageService.deleteFile(filePath);
            }



        return originalName;
    }

    public List<String> setVideos(List<MultipartFile> files) throws Exception {
        ensureBucketExists();

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Nenhum arquivo enviado");
        }

        List<String> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            String objectName = setVideo(file);
            uploadedFiles.add(objectName);
        }

        return uploadedFiles;
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

    private MinioClient getClient() {
        if (this.minioClient == null) {
            throw new IllegalStateException("MinIO client is not initialized");
        }
        return this.minioClient;
    }
}