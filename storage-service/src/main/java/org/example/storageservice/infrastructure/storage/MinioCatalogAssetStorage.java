package org.example.storageservice.infrastructure.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import org.example.storageservice.application.port.out.CatalogAssetStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class MinioCatalogAssetStorage implements CatalogAssetStorage {
    private final MinioClient minioClient;
    private final MinioClient minioPublicClient;

    @Value("${storage.assets.bucket}")
    private String bucket;

    public MinioCatalogAssetStorage(
            @Qualifier("minioClient") MinioClient minioClient,
            @Qualifier("minioPublicClient") MinioClient minioPublicClient) {
        this.minioClient = minioClient;
        this.minioPublicClient = minioPublicClient;
    }

    @Override
    public String createUploadUrl(String objectKey, int expiresSeconds) {
        return presigned(Http.Method.PUT, objectKey, expiresSeconds);
    }

    @Override
    public String createDownloadUrl(String objectKey, int expiresSeconds) {
        return presigned(Http.Method.GET, objectKey, expiresSeconds);
    }

    @Override
    public StoredObject stat(String objectKey) {
        try {
            StatObjectResponse response = minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).object(objectKey).build());
            return new StoredObject(response.size(), response.contentType());
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot inspect catalog asset object", exception);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot delete catalog asset object", exception);
        }
    }

    private String presigned(Http.Method method, String objectKey, int expiresSeconds) {
        try {
            return minioPublicClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(method)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry(expiresSeconds)
                    .build());
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot create catalog asset URL", exception);
        }
    }
}
