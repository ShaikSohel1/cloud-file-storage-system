package com.sohel.cloudstorage.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sohel.cloudstorage.model.FileData;
import com.sohel.cloudstorage.repository.FileRepository;

@Service
public class FileService {

    private final String FOLDER_PATH = System.getProperty("user.dir") + "/uploads/";

    @Autowired
    private FileRepository fileRepository;

    // ✅ Upload
    public String uploadFile(MultipartFile file) throws IOException {

        String fileName = file.getOriginalFilename().replaceAll(" ", "_");

        File folder = new File(FOLDER_PATH);
        if (!folder.exists()) folder.mkdirs();

        String filePath = FOLDER_PATH + fileName;

        file.transferTo(new File(filePath));

        fileRepository.save(new FileData(fileName, filePath, file.getContentType()));

        return "File uploaded successfully";
    }

    // ✅ Download
    public byte[] downloadFile(String fileName) throws IOException {

        FileData fileData = fileRepository.findByName(fileName);

        if (fileData == null) {
            throw new RuntimeException("File not found");
        }

        return Files.readAllBytes(new File(fileData.getPath()).toPath());
    }

    // ✅ Get all files
    public List<FileData> getAllFiles() {
        return fileRepository.findAll();
    }

    // ✅ Delete file
    public String deleteFile(String fileName) {

        FileData fileData = fileRepository.findByName(fileName);

        if (fileData == null) {
            return "File not found";
        }

        File file = new File(fileData.getPath());
        if (file.exists()) file.delete();

        fileRepository.delete(fileData);

        return "File deleted successfully";
    }
}