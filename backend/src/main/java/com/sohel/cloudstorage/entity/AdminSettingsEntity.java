package com.sohel.cloudstorage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Table(name = "admin_settings")
public class AdminSettingsEntity {

    @Id
    private String settingKey;

    @Column(nullable = false)
    private String settingValue;

    private String description;
}
