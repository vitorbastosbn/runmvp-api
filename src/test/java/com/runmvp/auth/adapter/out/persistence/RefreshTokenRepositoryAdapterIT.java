package com.runmvp.auth.adapter.out.persistence;

import com.runmvp.BaseIntegrationTest;
import com.runmvp.auth.domain.RefreshToken;
import com.runmvp.auth.application.port.out.RefreshTokenRepository;
import com.runmvp.user.domain.User;
import com.runmvp.user.application.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class RefreshTokenRepositoryAdapterIT extends BaseIntegrationTest {

    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private UserRepository userRepository;

    private Long userId;

    @BeforeEach
    void createUser() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        User user = User.create("sub-rt-" + unique, "RT User", "rt-" + unique + "@x.com", null, "RT" + unique);
        userId = userRepository.save(user).getId();
    }

    @Test
    void save_thenFindByHash_returnsToken() {
        RefreshToken token = RefreshToken.issue(userId, 30);
        refreshTokenRepository.save(token);

        Optional<RefreshToken> found = refreshTokenRepository.findByTokenHash(token.getTokenHash());
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(userId);
        assertThat(found.get().isRevoked()).isFalse();
    }

    @Test
    void revoke_marksTokenRevoked() {
        RefreshToken token = RefreshToken.issue(userId, 30);
        refreshTokenRepository.save(token);

        refreshTokenRepository.revokeByTokenHash(token.getTokenHash());

        Optional<RefreshToken> found = refreshTokenRepository.findByTokenHash(token.getTokenHash());
        assertThat(found).isPresent();
        assertThat(found.get().isRevoked()).isTrue();
    }

    @Test
    void revokeAllByUserId_revokesAllTokens() {
        RefreshToken t1 = RefreshToken.issue(userId, 30);
        RefreshToken t2 = RefreshToken.issue(userId, 30);
        refreshTokenRepository.save(t1);
        refreshTokenRepository.save(t2);

        refreshTokenRepository.revokeAllByUserId(userId);

        assertThat(refreshTokenRepository.findByTokenHash(t1.getTokenHash())
            .map(RefreshToken::isRevoked)).contains(true);
        assertThat(refreshTokenRepository.findByTokenHash(t2.getTokenHash())
            .map(RefreshToken::isRevoked)).contains(true);
    }
}
