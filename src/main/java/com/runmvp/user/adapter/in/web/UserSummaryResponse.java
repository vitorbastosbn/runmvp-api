package com.runmvp.user.adapter.in.web;

import com.runmvp.user.domain.User;

public record UserSummaryResponse(Long id, String name, String avatarUrl, String publicCode) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
            user.getId(), user.getName(), user.getAvatarUrl(), user.getPublicCode()
        );
    }
}
