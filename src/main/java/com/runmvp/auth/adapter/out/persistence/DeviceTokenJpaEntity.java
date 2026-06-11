package com.runmvp.auth.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "device_tokens")
class DeviceTokenJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "fcm_token", nullable = false, unique = true, length = 512)
    private String fcmToken;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    static DeviceTokenJpaEntity of(Long userId, String fcmToken) {
        DeviceTokenJpaEntity e = new DeviceTokenJpaEntity();
        e.userId = userId;
        e.fcmToken = fcmToken;
        e.updatedAt = Instant.now();
        return e;
    }

    Long getUserId()   { return userId; }
    String getFcmToken() { return fcmToken; }
}
