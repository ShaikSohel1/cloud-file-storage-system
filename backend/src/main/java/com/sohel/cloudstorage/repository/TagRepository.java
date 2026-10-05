package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.TagEntity;
import com.sohel.cloudstorage.entity.UserEntity;

public interface TagRepository extends JpaRepository<TagEntity, UUID> {
    List<TagEntity> findByOwner(UserEntity owner);
    Optional<TagEntity> findByNameAndOwner(String name, UserEntity owner);
}
