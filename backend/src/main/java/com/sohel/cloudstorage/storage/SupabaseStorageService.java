package com.sohel.cloudstorage.storage;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("supabaseStorageService")
public class SupabaseStorageService implements StorageService {

    @Value("${app.supabase.url:https://sqatowxytdyauwqlmkcx.supabase.co}")
    private String supabaseUrl;

    @Value("${app.supabase.key:}")
    private String supabaseKey;

    @Value("${app.supabase.bucket:cloudstorage}")
    private String bucketName;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Override
    public String uploadFile(MultipartFile file, String destinationPath) throws IOException {
        String objectKey = extractObjectKey(destinationPath);
        String uploadEndpoint = cleanUrl(supabaseUrl) + "/storage/v1/object/" + bucketName + "/" + encodePath(objectKey);

        log.info("Uploading file to Supabase Storage: bucket={}, key={}", bucketName, objectKey);

        byte[] fileBytes = file.getBytes();
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uploadEndpoint))
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("apikey", supabaseKey)
                    .header("Content-Type", contentType)
                    .header("x-upsert", "true")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Successfully uploaded file to Supabase: key={}", objectKey);
                return objectKey;
            } else {
                log.error("Failed to upload to Supabase Storage. Status: {}, Response: {}", response.statusCode(), response.body());
                throw new IOException("Supabase Storage upload failed with status " + response.statusCode() + ": " + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Upload interrupted: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadFile(String path) throws IOException {
        // Fallback: check if exists locally first
        File localFile = new File(path);
        if (localFile.exists() && localFile.isFile()) {
            return Files.readAllBytes(localFile.toPath());
        }

        String objectKey = extractObjectKey(path);
        String downloadUrl = cleanUrl(supabaseUrl) + "/storage/v1/object/public/" + bucketName + "/" + encodePath(objectKey);

        log.info("Downloading file from Supabase Storage: key={}", objectKey);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(downloadUrl))
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("apikey", supabaseKey)
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                log.warn("Supabase download returned status: {} for key: {}", response.statusCode(), objectKey);
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Download interrupted: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFile(String path) {
        // If file exists locally, delete it
        File localFile = new File(path);
        if (localFile.exists()) {
            localFile.delete();
        }

        String objectKey = extractObjectKey(path);
        String deleteUrl = cleanUrl(supabaseUrl) + "/storage/v1/object/" + bucketName + "/" + encodePath(objectKey);

        log.info("Deleting file from Supabase Storage: key={}", objectKey);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(deleteUrl))
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("apikey", supabaseKey)
                    .DELETE()
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            log.error("Failed to delete file from Supabase: key={}, error={}", objectKey, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean renameFile(String oldPath, String newPath) {
        String oldKey = extractObjectKey(oldPath);
        String newKey = extractObjectKey(newPath);

        log.info("Renaming/moving file in Supabase: {} -> {}", oldKey, newKey);

        String moveUrl = cleanUrl(supabaseUrl) + "/storage/v1/object/move";
        String payload = String.format("{\"bucketId\":\"%s\",\"sourceKey\":\"%s\",\"destinationKey\":\"%s\"}",
                bucketName, oldKey, newKey);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(moveUrl))
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("apikey", supabaseKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            log.error("Failed to rename file in Supabase: error={}", e.getMessage());
            return false;
        }
    }

    private String extractObjectKey(String path) {
        if (path == null) return "";
        // Strip common prefix like "/Users/.../uploads/" or "uploads/"
        String marker = "uploads/";
        int idx = path.indexOf(marker);
        if (idx >= 0) {
            return path.substring(idx + marker.length()).replaceAll("^/+", "");
        }
        return path.replaceAll("^/+", "");
    }

    private String encodePath(String path) {
        String[] parts = path.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append("/");
            sb.append(URLEncoder.encode(parts[i], StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return sb.toString();
    }

    private String cleanUrl(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
