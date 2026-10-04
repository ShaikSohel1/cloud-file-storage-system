package com.sohel.cloudstorage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.RefreshTokenEntity;
import com.sohel.cloudstorage.entity.UserEntity;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByToken(String token);
    void deleteByUser(UserEntity user);
    void deleteByToken(String token);
}
