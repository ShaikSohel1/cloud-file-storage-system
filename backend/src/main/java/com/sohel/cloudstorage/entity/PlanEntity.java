package com.sohel.cloudstorage.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "plans")
public class PlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name; // FREE, STARTER, PROFESSIONAL, BUSINESS, ENTERPRISE

    private String description;

    @Column(nullable = false)
    private long maxStorage;

    @Column(nullable = false)
    private int maxUsers;

    private boolean customDomainAllowed;
    private boolean whiteLabelAllowed;
    private boolean apiAccessAllowed;

    @Column(nullable = false)
    private long monthlyPrice; // Stored in cents
}
