package org.example.storageservice.infrastructure.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketCorsArgs;
import io.minio.messages.CORSConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MinioBucketInitializer {
    private static final Logger log = LoggerFactory.getLogger(MinioBucketInitializer.class);

    private final MinioClient minioClient;

    @Value("${storage.assets.bucket}")
    private String bucket;

    @Value("${security.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Value("${storage.assets.configure-cors:false}")
    private boolean configureCors;

    public MinioBucketInitializer(@Qualifier("minioClient") MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureBucketExists() {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
            if (configureCors) {
                configureCors();
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot initialize catalog asset bucket", exception);
        }
    }

    private void configureCors() {
        try {
            minioClient.setBucketCors(SetBucketCorsArgs.builder()
                    .bucket(bucket)
                    .config(new CORSConfiguration(List.of(new CORSConfiguration.CORSRule(
                            List.of("*"),
                            List.of("GET", "PUT", "HEAD"),
                            Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(origin -> !origin.isBlank()).toList(),
                            List.of("ETag"),
                            "catalog-assets",
                            3600))))
                    .build());
        } catch (Exception exception) {
            log.warn("Catalog asset bucket CORS could not be configured through the S3 API. "
                    + "Configure CORS at the object storage gateway or ingress if browser uploads require it.", exception);
        }
    }
}
