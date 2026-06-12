package com.runmvp.user.application.usecase;

import com.runmvp.auth.application.port.out.DeviceTokenRepository;
import com.runmvp.user.application.port.in.RegisterDeviceTokenUseCase;
import org.springframework.stereotype.Service;

@Service
public class RegisterDeviceTokenUseCaseImpl implements RegisterDeviceTokenUseCase {
    private final DeviceTokenRepository deviceTokenRepository;

    public RegisterDeviceTokenUseCaseImpl(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Override
    public void execute(Command command) {
        deviceTokenRepository.upsert(command.userId(), command.fcmToken());
    }
}
