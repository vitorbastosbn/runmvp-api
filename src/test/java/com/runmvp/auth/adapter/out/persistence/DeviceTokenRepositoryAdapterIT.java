package com.runmvp.auth.adapter.out.persistence;

import com.runmvp.BaseIntegrationTest;
import com.runmvp.auth.application.port.out.DeviceTokenRepository;
import com.runmvp.user.domain.User;
import com.runmvp.user.application.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class DeviceTokenRepositoryAdapterIT extends BaseIntegrationTest {

    @Autowired private DeviceTokenRepository deviceTokenRepository;
    @Autowired private UserRepository userRepository;

    private Long userId;

    @BeforeEach
    void createUser() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        User user = User.create("sub-dt-" + unique, "DT User", "dt-" + unique + "@x.com", null, "DT" + unique);
        userId = userRepository.save(user).getId();
    }

    @Test
    void upsert_savesToken() {
        deviceTokenRepository.upsert(userId, "fcm-token-abc");
        assertThat(deviceTokenRepository.findByUserId(userId))
            .contains("fcm-token-abc");
    }

    @Test
    void upsert_sameToken_doesNotDuplicate() {
        deviceTokenRepository.upsert(userId, "fcm-token-xyz");
        deviceTokenRepository.upsert(userId, "fcm-token-xyz");
        assertThat(deviceTokenRepository.findByUserId(userId))
            .hasSize(1);
    }

    @Test
    void deleteByFcmToken_removesToken() {
        deviceTokenRepository.upsert(userId, "fcm-to-delete");
        deviceTokenRepository.deleteByFcmToken("fcm-to-delete");
        assertThat(deviceTokenRepository.findByUserId(userId)).isEmpty();
    }

    @Test
    void deleteAllByUserId_removesAll() {
        deviceTokenRepository.upsert(userId, "token-1");
        deviceTokenRepository.upsert(userId, "token-2");
        deviceTokenRepository.deleteAllByUserId(userId);
        assertThat(deviceTokenRepository.findByUserId(userId)).isEmpty();
    }
}
