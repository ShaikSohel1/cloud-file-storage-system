package com.sohel.cloudstorage.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.sohel.cloudstorage.dto.response.FolderResponse;
import com.sohel.cloudstorage.entity.FolderEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FolderMapper {

    @Mapping(target = "parentFolderId", source = "parentFolder.id")
    FolderResponse toResponse(FolderEntity entity);
}
