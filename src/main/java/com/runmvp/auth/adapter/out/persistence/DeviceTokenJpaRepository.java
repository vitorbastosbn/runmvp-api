package com.runmvp.auth.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

interface DeviceTokenJpaRepository extends JpaRepository<DeviceTokenJpaEntity, Long> {
    List<DeviceTokenJpaEntity> findByUserId(Long userId);
    boolean existsByFcmToken(String fcmToken);
    void deleteByFcmToken(String fcmToken);
    void deleteByUserId(Long userId);
}
