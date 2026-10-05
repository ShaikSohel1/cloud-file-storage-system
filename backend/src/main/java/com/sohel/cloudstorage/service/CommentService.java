package com.sohel.cloudstorage.service;

import java.util.List;
import java.util.UUID;

import com.sohel.cloudstorage.dto.request.CommentRequest;
import com.sohel.cloudstorage.dto.response.CommentResponse;

public interface CommentService {
    CommentResponse addComment(String username, CommentRequest request);
    List<CommentResponse> getFileComments(String username, UUID fileId);
    String deleteComment(String username, UUID commentId);
}
