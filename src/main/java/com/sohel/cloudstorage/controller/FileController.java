package com.sohel.cloudstorage.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sohel.cloudstorage.model.FileData;
import com.sohel.cloudstorage.service.FileService;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        return fileService.uploadFile(file);
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<byte[]> download(@PathVariable String fileName) throws IOException {

        byte[] data = fileService.downloadFile(fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + fileName)
                .body(data);
    }

    @GetMapping("/view/{fileName:.+}")
    public ResponseEntity<byte[]> view(@PathVariable String fileName) throws IOException {

        byte[] data = fileService.downloadFile(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @GetMapping
    public List<FileData> listFiles() {
        return fileService.getAllFiles();
    }

    @DeleteMapping("/{fileName}")
    public String delete(@PathVariable String fileName) {
        return fileService.deleteFile(fileName);
    }

    @PutMapping("/rename")
    public String rename(
            @RequestParam String oldName,
            @RequestParam String newName) {

        return fileService.renameFile(oldName, newName);
    }
}