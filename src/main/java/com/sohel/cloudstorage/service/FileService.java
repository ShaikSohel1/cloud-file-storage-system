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

    public String uploadFile(MultipartFile file) throws IOException {

        String fileName = file.getOriginalFilename().replaceAll(" ", "_");

        File folder = new File(FOLDER_PATH);
        if (!folder.exists()) folder.mkdirs();

        String filePath = FOLDER_PATH + fileName;

        file.transferTo(new File(filePath));

        fileRepository.save(new FileData(fileName, filePath, file.getContentType()));

        return "File uploaded successfully";
    }

    public byte[] downloadFile(String fileName) throws IOException {

        FileData fileData = fileRepository.findByName(fileName);

        if (fileData == null) {
            throw new RuntimeException("File not found");
        }

        return Files.readAllBytes(new File(fileData.getPath()).toPath());
    }

    public List<FileData> getAllFiles() {
        return fileRepository.findAll();
    }


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
    public String renameFile(String oldName, String newName) {

    FileData fileData = fileRepository.findByName(oldName);

    if (fileData == null) {
        return "File not found";
    }

    File oldFile = new File(fileData.getPath());

    String newPath = FOLDER_PATH + newName;

    File newFile = new File(newPath);

    if (oldFile.renameTo(newFile)) {

        fileData.setName(newName);
        fileData.setPath(newPath);

        fileRepository.save(fileData);

        return "File renamed successfully";
    }

    return "Failed to rename file";
}
}
