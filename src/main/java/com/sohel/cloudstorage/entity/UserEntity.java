package com.sohel.cloudstorage.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sohel.cloudstorage.enums.AccountStatus;
import com.sohel.cloudstorage.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "profile_photo_url")
    private String profilePhoto;

    @Column(nullable = false)
    private boolean emailVerified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_organization_id")
    private OrganizationEntity currentOrganization;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;

    @Column(nullable = false)
    private Long storageUsed;

    @Column(nullable = false)
    private Long storageLimit;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.role == null) {
            this.role = Role.ROLE_USER;
        }
        if (this.status == null) {
            this.status = AccountStatus.PENDING_VERIFICATION;
        }
        if (this.storageUsed == null) {
            this.storageUsed = 0L;
        }
        if (this.storageLimit == null) {
            // Default 15 GB = 15 * 1024 * 1024 * 1024 bytes
            this.storageLimit = 16106127360L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
