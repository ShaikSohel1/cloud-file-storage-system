package com.sohel.cloudstorage.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Azure Blob Storage implementation of StorageService.
 * Connects to Azure Blob Storage using connection string and manages blob lifecycle.
 */
@Slf4j
@Service("azureBlobStorageService")
public class AzureBlobStorageService implements StorageService {

    @Value("${azure.storage.connection-string:${AZURE_STORAGE_CONNECTION_STRING:}}")
    private String connectionString;

    @Value("${azure.storage.container-name:${AZURE_STORAGE_CONTAINER_NAME:cloudstorage}}")
    private String containerName;

    private BlobContainerClient containerClient;

    @PostConstruct
    public void init() {
        if (connectionString != null && !connectionString.isBlank()) {
            try {
                BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                        .connectionString(connectionString)
                        .buildClient();
                containerClient = serviceClient.getBlobContainerClient(containerName);
                if (!containerClient.exists()) {
                    containerClient.create();
                    log.info("Created Azure Blob Storage container: {}", containerName);
                } else {
                    log.info("Connected to Azure Blob Storage container: {}", containerName);
                }
            } catch (Exception e) {
                log.warn("Failed to initialize Azure Blob Storage client: {}. Azure storage calls will fail if invoked.", e.getMessage());
            }
        } else {
            log.info("Azure Storage connection string not configured. AzureBlobStorageService initialized in standby mode.");
        }
    }

    private String sanitizeBlobName(String destinationPath) {
        if (destinationPath == null) return "unnamed_blob";
        // Remove leading slashes or Windows/Unix disk prefixes
        String name = destinationPath.replaceAll("^[a-zA-Z]:[/\\\\]", "").replace("\\", "/");
        if (name.startsWith("/")) name = name.substring(1);
        if (name.startsWith("uploads/")) name = name.substring("uploads/".length());
        return name;
    }

    @Override
    public String uploadFile(MultipartFile file, String destinationPath) throws IOException {
        ensureClientInitialized();
        String blobName = sanitizeBlobName(destinationPath);
        log.info("Uploading file to Azure Blob Storage: container={}, blob={}", containerName, blobName);

        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            BinaryData data = BinaryData.fromBytes(file.getBytes());

            blobClient.upload(data, true);

            if (file.getContentType() != null) {
                BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(file.getContentType());
                blobClient.setHttpHeaders(headers);
            }

            log.info("Successfully uploaded blob to Azure: {}", blobName);
            return blobName;
        } catch (Exception e) {
            log.error("Azure Blob upload failed for path {}: {}", destinationPath, e.getMessage());
            throw new IOException("Azure Blob upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadFile(String path) throws IOException {
        ensureClientInitialized();
        String blobName = sanitizeBlobName(path);
        log.info("Downloading file from Azure Blob Storage: blob={}", blobName);

        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            if (!blobClient.exists()) {
                log.warn("Blob not found in Azure: {}", blobName);
                return null;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blobClient.downloadStream(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Azure Blob download failed for {}: {}", path, e.getMessage());
            throw new IOException("Azure Blob download failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFile(String path) {
        if (containerClient == null) return false;
        String blobName = sanitizeBlobName(path);
        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            boolean deleted = blobClient.deleteIfExists();
            log.info("Deleted Azure blob {}: {}", blobName, deleted);
            return deleted;
        } catch (Exception e) {
            log.error("Failed to delete Azure blob {}: {}", blobName, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean renameFile(String oldPath, String newPath) {
        if (containerClient == null) return false;
        String oldBlobName = sanitizeBlobName(oldPath);
        String newBlobName = sanitizeBlobName(newPath);

        try {
            BlobClient sourceClient = containerClient.getBlobClient(oldBlobName);
            if (!sourceClient.exists()) return false;

            BlobClient destClient = containerClient.getBlobClient(newBlobName);
            destClient.copyFromUrl(sourceClient.getBlobUrl());
            sourceClient.delete();

            log.info("Renamed Azure blob from {} to {}", oldBlobName, newBlobName);
            return true;
        } catch (Exception e) {
            log.error("Failed to rename Azure blob from {} to {}: {}", oldBlobName, newBlobName, e.getMessage());
            return false;
        }
    }

    private void ensureClientInitialized() throws IOException {
        if (containerClient == null) {
            if (connectionString != null && !connectionString.isBlank()) {
                init();
            }
            if (containerClient == null) {
                throw new IOException("Azure Blob Storage client is not initialized. Please configure AZURE_STORAGE_CONNECTION_STRING.");
            }
        }
    }
}
