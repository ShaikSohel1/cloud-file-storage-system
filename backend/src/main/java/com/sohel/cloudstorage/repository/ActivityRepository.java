package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.ActivityEntity;
import com.sohel.cloudstorage.entity.UserEntity;

public interface ActivityRepository extends JpaRepository<ActivityEntity, UUID> {
    List<ActivityEntity> findByActorOrderByTimestampDesc(UserEntity actor, Pageable pageable);
}
