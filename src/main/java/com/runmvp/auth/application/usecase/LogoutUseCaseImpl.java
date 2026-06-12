package com.runmvp.auth.application.usecase;

import com.runmvp.auth.application.port.in.LogoutUseCase;
import com.runmvp.auth.application.port.out.DeviceTokenRepository;
import com.runmvp.auth.application.port.out.RefreshTokenRepository;
import com.runmvp.auth.domain.RefreshToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutUseCaseImpl implements LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final DeviceTokenRepository deviceTokenRepository;

    public LogoutUseCaseImpl(
            RefreshTokenRepository refreshTokenRepository,
            DeviceTokenRepository deviceTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Override
    @Transactional
    public void execute(Command command) {
        refreshTokenRepository.revokeByTokenHash(RefreshToken.hash(command.refreshToken()));
        if (command.fcmToken() != null && !command.fcmToken().isBlank()) {
            deviceTokenRepository.deleteByFcmToken(command.fcmToken());
        }
    }
}
