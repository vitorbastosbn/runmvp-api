package com.runmvp.auth.application.usecase;

import com.runmvp.auth.application.port.in.RefreshTokenUseCase;
import com.runmvp.auth.application.port.out.JwtService;
import com.runmvp.auth.application.port.out.RefreshTokenRepository;
import com.runmvp.auth.domain.RefreshToken;
import com.runmvp.shared.config.JwtProperties;
import com.runmvp.shared.exception.BusinessException;
import com.runmvp.shared.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtService jwtService;

    private RefreshTokenUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new RefreshTokenUseCaseImpl(
            refreshTokenRepository,
            jwtService,
            new JwtProperties("secret", 900L, 30L)
        );
    }

    @Test
    void execute_validToken_returnsNewTokensAndRevokesOld() {
        String rawToken = "refresh-token";
        String tokenHash = RefreshToken.hash(rawToken);
        RefreshToken stored = RefreshToken.reconstitute(
            1L,
            42L,
            tokenHash,
            Instant.now().plusSeconds(86400),
            null,
            Instant.now()
        );
        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(stored));
        when(jwtService.generateAccessToken(42L)).thenReturn("new-access");

        RefreshTokenUseCase.Result result =
            useCase.execute(new RefreshTokenUseCase.Command(rawToken));

        assertThat(result.accessToken()).isEqualTo("new-access");
        assertThat(result.newRefreshToken()).isNotBlank();
        verify(refreshTokenRepository).revokeByTokenHash(tokenHash);
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void execute_unknownToken_throwsInvalid() {
        when(refreshTokenRepository.findByTokenHash(RefreshToken.hash("unknown"))).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            useCase.execute(new RefreshTokenUseCase.Command("unknown")))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.REFRESH_TOKEN_INVALID);
    }

    @Test
    void execute_revokedToken_throwsInvalid() {
        String rawToken = "revoked-token";
        String tokenHash = RefreshToken.hash(rawToken);
        RefreshToken revoked = RefreshToken.reconstitute(
            1L,
            42L,
            tokenHash,
            Instant.now().plusSeconds(86400),
            Instant.now(),
            Instant.now()
        );
        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() ->
            useCase.execute(new RefreshTokenUseCase.Command(rawToken)))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.REFRESH_TOKEN_INVALID);
    }

    @Test
    void execute_expiredToken_throwsExpired() {
        String rawToken = "expired-token";
        String tokenHash = RefreshToken.hash(rawToken);
        RefreshToken expired = RefreshToken.reconstitute(
            1L,
            42L,
            tokenHash,
            Instant.now().minusSeconds(1),
            null,
            Instant.now()
        );
        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(expired));

        assertThatThrownBy(() ->
            useCase.execute(new RefreshTokenUseCase.Command(rawToken)))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }
}
