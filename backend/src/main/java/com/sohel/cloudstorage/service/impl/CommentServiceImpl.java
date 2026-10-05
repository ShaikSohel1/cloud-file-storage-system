package com.sohel.cloudstorage.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohel.cloudstorage.dto.request.CommentRequest;
import com.sohel.cloudstorage.dto.response.CommentResponse;
import com.sohel.cloudstorage.entity.CommentEntity;
import com.sohel.cloudstorage.entity.FileEntity;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.exception.ResourceNotFoundException;
import com.sohel.cloudstorage.mapper.CommentMapper;
import com.sohel.cloudstorage.repository.CommentRepository;
import com.sohel.cloudstorage.repository.FileRepository;
import com.sohel.cloudstorage.repository.UserRepository;
import com.sohel.cloudstorage.service.CommentService;
import com.sohel.cloudstorage.service.PermissionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentResponse addComment(String username, CommentRequest request) {
        UserEntity author = getUser(username);
        FileEntity file = fileRepository.findById(request.getFileId())
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        if (!permissionService.canReadUserFile(username, request.getFileId())) {
            throw new IllegalArgumentException("Unauthorized to comment on this file");
        }

        CommentEntity parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId()).orElse(null);
        }

        CommentEntity comment = CommentEntity.builder()
                .file(file)
                .author(author)
                .content(request.getContent())
                .parentComment(parentComment)
                .build();

        comment = commentRepository.save(comment);
        return commentMapper.toResponse(comment);
    }

    @Override
    public List<CommentResponse> getFileComments(String username, UUID fileId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        if (!permissionService.canReadUserFile(username, fileId)) {
            throw new IllegalArgumentException("Unauthorized to view comments");
        }

        return commentRepository.findByFileOrderByCreatedAtAsc(file).stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String deleteComment(String username, UUID commentId) {
        UserEntity user = getUser(username);
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized to delete this comment");
        }

        commentRepository.delete(comment);
        return "Comment deleted";
    }

    private UserEntity getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
