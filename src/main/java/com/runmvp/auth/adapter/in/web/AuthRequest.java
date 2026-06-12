package com.runmvp.auth.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public sealed interface AuthRequest {

    record Google(@NotBlank String idToken, String fcmToken) implements AuthRequest {}

    record Refresh(@NotBlank String refreshToken) implements AuthRequest {}

    record Logout(@NotBlank String refreshToken, String fcmToken) implements AuthRequest {}
}
