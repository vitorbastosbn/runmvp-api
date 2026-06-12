package com.runmvp.subscription.adapter.out.persistence;

import com.runmvp.subscription.domain.Subscription;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "subscriptions")
class SubscriptionJpaEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(name = "purchase_token", nullable = false) private String purchaseToken;
    @Column(name = "product_id", nullable = false) private String productId;
    @Column(name = "product_type", nullable = false, length = 20) private String productType;
    @Column(nullable = false, length = 20) private String status;
    @Column(nullable = false, length = 20) private String entitlement;
    @Column(nullable = false, length = 20) private String provider = "GOOGLE_PLAY";
    @Column(name = "started_at", nullable = false) private Instant startedAt;
    @Column(name = "expires_at") private Instant expiresAt;
    @Column(name = "auto_renewing", nullable = false) private boolean autoRenewing = false;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version
    @Column(nullable = false) private Long version = 0L;

    static SubscriptionJpaEntity fromDomain(Subscription s) {
        SubscriptionJpaEntity e = new SubscriptionJpaEntity();
        e.id = s.getId(); e.userId = s.getUserId();
        e.purchaseToken = s.getPurchaseToken();
        e.productId = s.getProductId();
        e.productType = s.getProductType().name();
        e.status = s.getStatus().name();
        e.entitlement = s.isEntitled() ? "PREMIUM_ACTIVE" : "FREE";
        e.startedAt = s.getCreatedAt();
        e.expiresAt = s.getExpiresAt();
        e.createdAt = s.getCreatedAt();
        e.updatedAt = s.getUpdatedAt();
        return e;
    }

    Subscription toDomain() {
        return Subscription.reconstitute(id, userId, purchaseToken, productId,
            Subscription.ProductType.valueOf(productType), Subscription.Status.valueOf(status),
            expiresAt, createdAt, updatedAt);
    }
}
