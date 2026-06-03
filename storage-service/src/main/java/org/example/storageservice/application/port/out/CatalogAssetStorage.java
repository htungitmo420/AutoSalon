package org.example.storageservice.application.port.out;

public interface CatalogAssetStorage {
    String createUploadUrl(String objectKey, int expiresSeconds);
    String createDownloadUrl(String objectKey, int expiresSeconds);
    StoredObject stat(String objectKey);
    void delete(String objectKey);

    record StoredObject(long sizeBytes, String contentType) {
    }
}
