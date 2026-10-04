package com.sohel.cloudstorage.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sohel.cloudstorage.dto.request.CommentRequest;
import com.sohel.cloudstorage.dto.response.ApiResponse;
import com.sohel.cloudstorage.dto.response.CommentResponse;
import com.sohel.cloudstorage.service.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            Principal principal,
            @Valid @RequestBody CommentRequest request) {
        CommentResponse response = commentService.addComment(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Comment added", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getFileComments(
            Principal principal,
            @RequestParam("fileId") UUID fileId) {
        List<CommentResponse> comments = commentService.getFileComments(principal.getName(), fileId);
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved", comments));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteComment(Principal principal, @PathVariable("id") UUID commentId) {
        String result = commentService.deleteComment(principal.getName(), commentId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted", result));
    }
}
