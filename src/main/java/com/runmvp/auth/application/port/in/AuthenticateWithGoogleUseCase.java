package com.runmvp.auth.application.port.in;

import com.runmvp.user.domain.User;

public interface AuthenticateWithGoogleUseCase {

    record Command(String idToken, String fcmToken) {}

    record Result(String accessToken, String refreshToken, User user, boolean isNewUser) {}

    Result execute(Command command);
}
