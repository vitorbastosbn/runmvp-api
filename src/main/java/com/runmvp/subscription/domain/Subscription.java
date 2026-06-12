package com.runmvp.subscription.domain;

import java.time.Instant;

public class Subscription {

    public enum Status { ACTIVE, CANCELLED, EXPIRED, ON_HOLD, PAUSED }
    public enum ProductType { MONTHLY, ANNUAL }

    private Long id;
    private Long userId;
    private String purchaseToken;
    private String productId;
    private ProductType productType;
    private Status status;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;

    private Subscription() {}

    public static Subscription create(Long userId, String purchaseToken,
                                      String productId, ProductType productType,
                                      Instant expiresAt) {
        Subscription s = new Subscription();
        s.userId = userId;
        s.purchaseToken = purchaseToken;
        s.productId = productId;
        s.productType = productType;
        s.status = Status.ACTIVE;
        s.expiresAt = expiresAt;
        s.createdAt = Instant.now();
        s.updatedAt = Instant.now();
        return s;
    }

    public static Subscription reconstitute(Long id, Long userId, String purchaseToken,
            String productId, ProductType productType, Status status,
            Instant expiresAt, Instant createdAt, Instant updatedAt) {
        Subscription s = new Subscription();
        s.id = id; s.userId = userId;
        s.purchaseToken = purchaseToken;
        s.productId = productId; s.productType = productType;
        s.status = status; s.expiresAt = expiresAt;
        s.createdAt = createdAt; s.updatedAt = updatedAt;
        return s;
    }

    public void renew(Instant newExpiresAt) {
        this.status = Status.ACTIVE;
        this.expiresAt = newExpiresAt;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = Status.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public void expire() {
        this.status = Status.EXPIRED;
        this.updatedAt = Instant.now();
    }

    public boolean isEntitled() {
        return (status == Status.ACTIVE || status == Status.CANCELLED)
            && expiresAt != null && expiresAt.isAfter(Instant.now());
    }

    public Long getId()                 { return id; }
    public Long getUserId()             { return userId; }
    public String getPurchaseToken()    { return purchaseToken; }
    public String getProductId()        { return productId; }
    public ProductType getProductType() { return productType; }
    public Status getStatus()           { return status; }
    public Instant getExpiresAt()       { return expiresAt; }
    public Instant getCreatedAt()       { return createdAt; }
    public Instant getUpdatedAt()       { return updatedAt; }
    public void setId(Long id)          { this.id = id; }
}
