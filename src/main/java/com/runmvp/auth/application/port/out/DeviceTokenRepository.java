package com.runmvp.auth.application.port.out;

import java.util.List;

public interface DeviceTokenRepository {
    void upsert(Long userId, String fcmToken);
    List<String> findByUserId(Long userId);
    void deleteByFcmToken(String fcmToken);
    void deleteAllByUserId(Long userId);
}
