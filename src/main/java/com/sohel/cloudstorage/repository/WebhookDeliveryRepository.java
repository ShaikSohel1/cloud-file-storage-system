package com.sohel.cloudstorage.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sohel.cloudstorage.entity.WebhookDeliveryEntity;
import com.sohel.cloudstorage.entity.WebhookEntity;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDeliveryEntity, UUID> {
    List<WebhookDeliveryEntity> findByWebhookOrderByDeliveredAtDesc(WebhookEntity webhook);
}
