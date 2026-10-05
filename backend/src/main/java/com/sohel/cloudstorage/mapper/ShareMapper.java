package com.sohel.cloudstorage.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.sohel.cloudstorage.dto.response.InvitationResponse;
import com.sohel.cloudstorage.dto.response.ShareLinkResponse;
import com.sohel.cloudstorage.dto.response.SharedResourceResponse;
import com.sohel.cloudstorage.entity.InvitationEntity;
import com.sohel.cloudstorage.entity.ShareLinkEntity;
import com.sohel.cloudstorage.entity.SharedResourceEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class, FileMapper.class, FolderMapper.class})
public interface ShareMapper {
    SharedResourceResponse toResourceResponse(SharedResourceEntity entity);

    @Mapping(target = "passwordProtected", expression = "java(entity.getPasswordHash() != null && !entity.getPasswordHash().isEmpty())")
    ShareLinkResponse toLinkResponse(ShareLinkEntity entity);

    @Mapping(target = "workspaceName", source = "workspace.name")
    @Mapping(target = "fileName", source = "file.name")
    @Mapping(target = "folderName", source = "folder.name")
    InvitationResponse toInvitationResponse(InvitationEntity entity);
}
