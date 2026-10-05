package com.sohel.cloudstorage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.PasswordResetTokenEntity;
import com.sohel.cloudstorage.entity.UserEntity;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {
    Optional<PasswordResetTokenEntity> findByToken(String token);
    void deleteByUser(UserEntity user);
}
