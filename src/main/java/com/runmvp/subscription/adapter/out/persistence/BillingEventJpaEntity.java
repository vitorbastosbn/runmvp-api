package com.runmvp.subscription.adapter.out.persistence;

import com.runmvp.subscription.application.port.out.BillingEventRepository;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "play_billing_events")
class BillingEventJpaEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "message_id", nullable = false, unique = true) private String messageId;
    @Column(name = "purchase_token") private String purchaseToken;
    @Column(name = "notification_type") private Integer notificationType;
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb") private String payload;
    @Column(name = "received_at", nullable = false) private Instant receivedAt;

    static BillingEventJpaEntity from(BillingEventRepository.Event e) {
        BillingEventJpaEntity entity = new BillingEventJpaEntity();
        entity.messageId = UUID.randomUUID().toString();
        entity.purchaseToken = e.purchaseToken();
        entity.payload = e.rawPayload() != null ? e.rawPayload() : "{}";
        entity.receivedAt = e.receivedAt();
        return entity;
    }
}
