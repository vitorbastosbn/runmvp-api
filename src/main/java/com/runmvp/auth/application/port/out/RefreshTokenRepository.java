package com.runmvp.auth.application.port.out;

import com.runmvp.auth.domain.RefreshToken;
import java.util.Optional;

public interface RefreshTokenRepository {
    void save(RefreshToken token);
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    void revokeByTokenHash(String tokenHash);
    void revokeAllByUserId(Long userId);
}
