package com.sohel.cloudstorage.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.sohel.cloudstorage.dto.response.AuditLogResponse;
import com.sohel.cloudstorage.dto.response.FavoriteResponse;
import com.sohel.cloudstorage.dto.response.FileVersionResponse;
import com.sohel.cloudstorage.dto.response.SecurityLogResponse;
import com.sohel.cloudstorage.dto.response.TagResponse;
import com.sohel.cloudstorage.dto.response.TrashItemResponse;
import com.sohel.cloudstorage.entity.AuditLogEntity;
import com.sohel.cloudstorage.entity.FavoriteEntity;
import com.sohel.cloudstorage.entity.FileVersionEntity;
import com.sohel.cloudstorage.entity.SecurityLogEntity;
import com.sohel.cloudstorage.entity.TagEntity;
import com.sohel.cloudstorage.entity.TrashItemEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class, FileMapper.class, FolderMapper.class, WorkspaceMapper.class})
public interface Phase5Mapper {

    @Mapping(target = "fileId", source = "file.id")
    FileVersionResponse toFileVersionResponse(FileVersionEntity entity);

    @Mapping(target = "workspaceName", source = "workspace.name")
    TrashItemResponse toTrashItemResponse(TrashItemEntity entity);

    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "workspaceName", source = "workspace.name")
    AuditLogResponse toAuditLogResponse(AuditLogEntity entity);

    @Mapping(target = "userEmail", source = "user.email")
    SecurityLogResponse toSecurityLogResponse(SecurityLogEntity entity);

    TagResponse toTagResponse(TagEntity entity);

    FavoriteResponse toFavoriteResponse(FavoriteEntity entity);
}
