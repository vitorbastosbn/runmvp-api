package com.runmvp.auth.domain;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

public class RefreshToken {

    private Long id;
    private Long userId;
    private String rawToken;
    private String tokenHash;
    private Instant expiresAt;
    private Instant revokedAt;
    private Instant createdAt;

    private RefreshToken() {}

    public static RefreshToken issue(Long userId, long expirationDays) {
        String raw = generateRawToken();
        RefreshToken rt = new RefreshToken();
        rt.userId = userId;
        rt.rawToken = raw;
        rt.tokenHash = hash(raw);
        rt.expiresAt = Instant.now().plusSeconds(expirationDays * 86_400L);
        rt.createdAt = Instant.now();
        return rt;
    }

    public static RefreshToken reconstitute(Long id, Long userId, String tokenHash,
                                            Instant expiresAt, Instant revokedAt,
                                            Instant createdAt) {
        RefreshToken rt = new RefreshToken();
        rt.id = id;
        rt.userId = userId;
        rt.tokenHash = tokenHash;
        rt.expiresAt = expiresAt;
        rt.revokedAt = revokedAt;
        rt.createdAt = createdAt;
        return rt;
    }

    public boolean isRevoked()  { return revokedAt != null; }
    public boolean isExpired()  { return Instant.now().isAfter(expiresAt); }

    public Long getId()           { return id; }
    public Long getUserId()       { return userId; }
    public String getRawToken()   { return rawToken; }
    public String getTokenHash()  { return tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public Instant getCreatedAt() { return createdAt; }

    private static String generateRawToken() {
        byte[] bytes = new byte[48];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String raw) {
        try {
            java.security.MessageDigest digest =
                java.security.MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(encoded);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
