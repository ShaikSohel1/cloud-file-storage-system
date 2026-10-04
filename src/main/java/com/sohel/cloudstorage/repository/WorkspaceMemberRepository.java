package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.entity.WorkspaceEntity;
import com.sohel.cloudstorage.entity.WorkspaceMemberEntity;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMemberEntity, Long> {
    List<WorkspaceMemberEntity> findByWorkspace(WorkspaceEntity workspace);
    List<WorkspaceMemberEntity> findByUser(UserEntity user);
    Optional<WorkspaceMemberEntity> findByWorkspaceAndUser(WorkspaceEntity workspace, UserEntity user);
}
