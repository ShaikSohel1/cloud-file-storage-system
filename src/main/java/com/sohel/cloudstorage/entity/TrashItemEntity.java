package com.sohel.cloudstorage.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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
@Table(name = "trash_items")
public class TrashItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private FileEntity file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private FolderEntity folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_folder_id")
    private FolderEntity originalFolder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private WorkspaceEntity workspace;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by_id", nullable = false)
    private UserEntity deletedBy;

    @Column(nullable = false)
    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    @PrePersist
    protected void onCreate() {
        this.deletedAt = LocalDateTime.now();
        if (this.expirationDate == null) {
            this.expirationDate = this.deletedAt.plusDays(30);
        }
    }
}
