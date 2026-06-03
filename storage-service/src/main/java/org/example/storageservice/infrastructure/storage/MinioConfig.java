package org.example.storageservice.infrastructure.storage;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class MinioConfig {
    @Bean
    MinioClient minioClient(
            @Value("${storage.assets.endpoint}") String endpoint,
            @Value("${storage.assets.access-key}") String accessKey,
            @Value("${storage.assets.secret-key}") String secretKey,
            @Value("${storage.assets.region}") String region) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .region(region)
                .build();
    }

    @Bean
    @Qualifier("minioPublicClient")
    MinioClient minioPublicClient(
            @Value("${storage.assets.public-endpoint}") String endpoint,
            @Value("${storage.assets.access-key}") String accessKey,
            @Value("${storage.assets.secret-key}") String secretKey,
            @Value("${storage.assets.region}") String region) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .region(region)
                .build();
    }
}
