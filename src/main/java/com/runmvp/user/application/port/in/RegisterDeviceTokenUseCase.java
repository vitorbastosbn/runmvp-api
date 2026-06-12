package com.runmvp.user.application.port.in;

public interface RegisterDeviceTokenUseCase {
    record Command(Long userId, String fcmToken) {}
    void execute(Command command);
}
