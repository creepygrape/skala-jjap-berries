package com.jjap.berries.user.service;

import com.jjap.berries.auth.repository.RefreshTokenRepository;
import com.jjap.berries.global.exception.BusinessException;
import com.jjap.berries.global.exception.ErrorCode;
import com.jjap.berries.user.domain.User;
import com.jjap.berries.user.dto.UpdateUserRequest;
import com.jjap.berries.user.dto.UserResponse;
import com.jjap.berries.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository users;
  private final RefreshTokenRepository refreshTokens;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  public UserResponse me(Long userId) {
    return UserResponse.from(user(userId));
  }

  @Transactional(readOnly = true)
  public UserResponse get(Long userId) {
    return UserResponse.from(user(userId));
  }

  @Transactional
  public UserResponse update(Long userId, UpdateUserRequest request) {
    User user = user(userId);
    if (request.nickname() == null
        && request.profileImageUrl() == null
        && request.password() == null) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }
    if (request.nickname() != null && request.nickname().isBlank())
      throw new BusinessException(ErrorCode.INVALID_REQUEST);
    if (request.nickname() != null && users.existsByNicknameAndIdNot(request.nickname(), userId))
      throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
    user.updateProfile(request.nickname(), request.profileImageUrl());
    if (request.password() != null) {
      user.changePassword(passwordEncoder.encode(request.password()));
    }
    return UserResponse.from(user);
  }

  @Transactional
  public void withdraw(Long userId) {
    User user = user(userId);
    user.withdraw();
    refreshTokens.deleteByUserId(userId);
  }

  private User user(Long id) {
    return users.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }
}
