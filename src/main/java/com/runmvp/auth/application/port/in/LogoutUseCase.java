package com.runmvp.auth.application.port.in;

public interface LogoutUseCase {

    record Command(String refreshToken, String fcmToken) {}

    void execute(Command command);
}
