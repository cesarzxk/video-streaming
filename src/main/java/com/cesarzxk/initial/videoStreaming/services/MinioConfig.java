package com.cesarzxk.initial.videoStreaming.services;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Value("${minio.endpoint:http://192.168.31.2:9000}")
    private String minioEndpoint;

    @Value("${minio.accessKey:admin}")
    private String accessKey;

    @Value("${minio.secretKey:admin}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioEndpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}

