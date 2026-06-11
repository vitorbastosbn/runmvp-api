package com.runmvp.auth.adapter.out.persistence;

import com.runmvp.auth.application.port.out.DeviceTokenRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
class DeviceTokenRepositoryAdapter implements DeviceTokenRepository {

    private final DeviceTokenJpaRepository jpa;

    DeviceTokenRepositoryAdapter(DeviceTokenJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public void upsert(Long userId, String fcmToken) {
        if (!jpa.existsByFcmToken(fcmToken)) {
            jpa.save(DeviceTokenJpaEntity.of(userId, fcmToken));
        }
    }

    @Override
    public List<String> findByUserId(Long userId) {
        return jpa.findByUserId(userId).stream()
            .map(DeviceTokenJpaEntity::getFcmToken)
            .toList();
    }

    @Override
    @Transactional
    public void deleteByFcmToken(String fcmToken) {
        jpa.deleteByFcmToken(fcmToken);
    }

    @Override
    @Transactional
    public void deleteAllByUserId(Long userId) {
        jpa.deleteByUserId(userId);
    }
}
