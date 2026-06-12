package com.runmvp.auth.application.port.in;

public interface RefreshTokenUseCase {

    record Command(String refreshToken) {}

    record Result(String accessToken, String newRefreshToken) {}

    Result execute(Command command);
}
