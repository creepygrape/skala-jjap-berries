package com.jjap.berries.user.dto;

import com.jjap.berries.user.domain.User;
import com.jjap.berries.user.domain.UserRole;
import com.jjap.berries.user.domain.UserStatus;
import java.time.LocalDateTime;

public record UserResponse(
    Long userId,
    String email,
    String nickname,
    String profileImageUrl,
    UserRole role,
    UserStatus status,
    LocalDateTime createdAt) {
  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        user.getProfileImageUrl(),
        user.getRole(),
        user.getStatus(),
        user.getCreatedAt());
  }
}
