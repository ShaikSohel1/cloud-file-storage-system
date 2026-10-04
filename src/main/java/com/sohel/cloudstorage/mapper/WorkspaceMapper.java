package com.sohel.cloudstorage.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.sohel.cloudstorage.dto.response.WorkspaceMemberResponse;
import com.sohel.cloudstorage.dto.response.WorkspaceResponse;
import com.sohel.cloudstorage.entity.WorkspaceEntity;
import com.sohel.cloudstorage.entity.WorkspaceMemberEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public interface WorkspaceMapper {
    WorkspaceResponse toResponse(WorkspaceEntity entity);
    WorkspaceMemberResponse toMemberResponse(WorkspaceMemberEntity entity);
}
