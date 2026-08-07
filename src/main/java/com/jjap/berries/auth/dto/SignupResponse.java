package com.jjap.berries.auth.dto;

import com.jjap.berries.user.domain.User;
import com.jjap.berries.user.domain.UserRole;

public record SignupResponse(Long userId, String email, String nickname, UserRole role) {

    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId(), user.getEmail(), user.getNickname(), user.getRole());
    }
}
