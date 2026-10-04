package com.sohel.cloudstorage.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.OrganizationEntity;
import com.sohel.cloudstorage.entity.SubscriptionEntity;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, UUID> {
    Optional<SubscriptionEntity> findByOrganization(OrganizationEntity organization);
    Optional<SubscriptionEntity> findByStripeSubscriptionId(String stripeSubscriptionId);
}
