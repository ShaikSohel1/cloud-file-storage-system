package com.sohel.cloudstorage.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sohel.cloudstorage.dto.response.StorageStatsResponse;
import com.sohel.cloudstorage.entity.FileEntity;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.repository.FileRepository;
import com.sohel.cloudstorage.repository.FolderRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.service.StatisticsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    @Override
    public StorageStatsResponse getStorageStats(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Long usedBytes = fileRepository.sumSizeByOwnerAndDeletedFalse(user);
        if (usedBytes == null) {
            usedBytes = 0L;
        }

        Long limitBytes = user.getStorageLimit();
        double percentage = limitBytes > 0 ? ((double) usedBytes / limitBytes) * 100 : 0.0;

        long totalFiles = fileRepository.countByOwnerAndDeletedFalse(user);
        long totalFolders = folderRepository.countByOwnerAndDeletedFalse(user);

        List<FileEntity> files = fileRepository.findByOwnerAndFolderIsNullAndDeletedFalse(user); // or all active files
        Map<String, Long> sizeByCategory = new HashMap<>();
        sizeByCategory.put("Images", 0L);
        sizeByCategory.put("Documents", 0L);
        sizeByCategory.put("Videos", 0L);
        sizeByCategory.put("Audio", 0L);
        sizeByCategory.put("Code", 0L);
        sizeByCategory.put("Archives", 0L);
        sizeByCategory.put("Other", 0L);

        for (FileEntity file : files) {
            long fileSize = file.getSize() != null ? file.getSize() : 0L;
            String category = categorizeFile(file.getType(), file.getExtension());
            sizeByCategory.put(category, sizeByCategory.getOrDefault(category, 0L) + fileSize);
        }

        return StorageStatsResponse.builder()
                .storageUsed(usedBytes)
                .storageLimit(limitBytes)
                .usagePercentage(Math.round(percentage * 100.0) / 100.0)
                .totalFiles(totalFiles)
                .totalFolders(totalFolders)
                .sizeByCategory(sizeByCategory)
                .build();
    }

    private String categorizeFile(String mimeType, String extension) {
        if (mimeType == null) mimeType = "";
        if (extension == null) extension = "";
        mimeType = mimeType.toLowerCase();
        extension = extension.toLowerCase();

        if (mimeType.startsWith("image/") || List.of("png", "jpg", "jpeg", "gif", "svg", "webp").contains(extension)) {
            return "Images";
        }
        if (mimeType.startsWith("video/") || List.of("mp4", "mkv", "avi", "mov").contains(extension)) {
            return "Videos";
        }
        if (mimeType.startsWith("audio/") || List.of("mp3", "wav", "flac").contains(extension)) {
            return "Audio";
        }
        if (mimeType.contains("pdf") || mimeType.contains("word") || mimeType.contains("excel") || List.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt").contains(extension)) {
            return "Documents";
        }
        if (List.of("zip", "tar", "gz", "7z", "rar").contains(extension)) {
            return "Archives";
        }
        if (List.of("java", "py", "js", "ts", "json", "html", "css", "xml", "md").contains(extension)) {
            return "Code";
        }
        return "Other";
    }
}
