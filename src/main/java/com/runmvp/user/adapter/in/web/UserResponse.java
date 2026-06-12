package com.runmvp.user.adapter.in.web;

import com.runmvp.user.domain.User;

public record UserResponse(
    Long id,
    String name,
    String email,
    String avatarUrl,
    String publicCode,
    String entitlement,
    String createdAt
) {
    public static UserResponse from(User user, String entitlement) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAvatarUrl(),
            user.getPublicCode(),
            entitlement,
            user.getCreatedAt().toString()
        );
    }
}
