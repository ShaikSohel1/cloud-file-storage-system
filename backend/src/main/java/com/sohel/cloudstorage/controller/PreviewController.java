package com.sohel.cloudstorage.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sohel.cloudstorage.service.PreviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/files/{fileId}/preview")
@RequiredArgsConstructor
public class PreviewController {

    private final PreviewService previewService;

    @GetMapping
    public ResponseEntity<Resource> getFilePreview(
            Principal principal,
            @PathVariable("fileId") UUID fileId) {
        Resource file = previewService.getFilePreview(principal.getName(), fileId);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                // A production app would probe content type dynamically here
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }
}
