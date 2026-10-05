package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.UserEntity;
import com.sohel.cloudstorage.entity.UserSessionEntity;

public interface UserSessionRepository extends JpaRepository<UserSessionEntity, Long> {
    List<UserSessionEntity> findByUser(UserEntity user);
    Optional<UserSessionEntity> findByUserAndRefreshToken(UserEntity user, String refreshToken);
    void deleteByUser(UserEntity user);
    void deleteByRefreshToken(String refreshToken);
}
