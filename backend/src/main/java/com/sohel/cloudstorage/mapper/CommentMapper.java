package com.sohel.cloudstorage.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.sohel.cloudstorage.dto.response.CommentResponse;
import com.sohel.cloudstorage.entity.CommentEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public interface CommentMapper {

    @Mapping(target = "fileId", source = "file.id")
    @Mapping(target = "parentCommentId", source = "parentComment.id")
    CommentResponse toResponse(CommentEntity entity);
}
