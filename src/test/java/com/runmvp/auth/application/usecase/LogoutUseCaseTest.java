package com.runmvp.auth.application.usecase;

import com.runmvp.auth.application.port.in.LogoutUseCase;
import com.runmvp.auth.application.port.out.DeviceTokenRepository;
import com.runmvp.auth.application.port.out.RefreshTokenRepository;
import com.runmvp.auth.domain.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private DeviceTokenRepository deviceTokenRepository;

    private LogoutUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new LogoutUseCaseImpl(refreshTokenRepository, deviceTokenRepository);
    }

    @Test
    void execute_revokesRefreshTokenAndDeletesDeviceToken() {
        useCase.execute(new LogoutUseCase.Command("refresh-token", "fcm-abc"));

        verify(refreshTokenRepository).revokeByTokenHash(RefreshToken.hash("refresh-token"));
        verify(deviceTokenRepository).deleteByFcmToken("fcm-abc");
    }

    @Test
    void execute_blankFcmToken_revokesRefreshTokenOnly() {
        useCase.execute(new LogoutUseCase.Command("refresh-token", " "));

        verify(refreshTokenRepository).revokeByTokenHash(RefreshToken.hash("refresh-token"));
        verify(deviceTokenRepository, never()).deleteByFcmToken(" ");
    }
}
