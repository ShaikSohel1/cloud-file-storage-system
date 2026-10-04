package com.sohel.cloudstorage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.EmailVerificationTokenEntity;
import com.sohel.cloudstorage.entity.UserEntity;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationTokenEntity, Long> {
    Optional<EmailVerificationTokenEntity> findByToken(String token);
    void deleteByUser(UserEntity user);
}
