package com.sohel.cloudstorage.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.sohel.cloudstorage.dto.response.ActivityResponse;
import com.sohel.cloudstorage.dto.response.NotificationResponse;
import com.sohel.cloudstorage.entity.ActivityEntity;
import com.sohel.cloudstorage.entity.NotificationEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public interface ActivityMapper {

    @Mapping(target = "workspaceName", source = "workspace.name")
    @Mapping(target = "fileName", source = "file.name")
    @Mapping(target = "folderName", source = "folder.name")
    ActivityResponse toActivityResponse(ActivityEntity entity);

    NotificationResponse toNotificationResponse(NotificationEntity entity);
}
