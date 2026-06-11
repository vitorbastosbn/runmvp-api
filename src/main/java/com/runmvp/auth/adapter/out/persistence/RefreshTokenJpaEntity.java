package com.runmvp.auth.adapter.out.persistence;

import com.runmvp.auth.domain.RefreshToken;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    static RefreshTokenJpaEntity fromDomain(RefreshToken t) {
        RefreshTokenJpaEntity e = new RefreshTokenJpaEntity();
        e.userId = t.getUserId();
        e.tokenHash = t.getTokenHash();
        e.expiresAt = t.getExpiresAt();
        e.revokedAt = t.getRevokedAt();
        e.createdAt = t.getCreatedAt() != null ? t.getCreatedAt() : Instant.now();
        return e;
    }

    RefreshToken toDomain() {
        return RefreshToken.reconstitute(id, userId, tokenHash, expiresAt, revokedAt, createdAt);
    }

    Long getId()    { return id; }
    Long getUserId() { return userId; }
}
