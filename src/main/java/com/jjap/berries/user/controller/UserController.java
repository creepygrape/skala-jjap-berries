package com.jjap.berries.user.controller;

import com.jjap.berries.global.common.response.ApiResponse;
import com.jjap.berries.global.security.CurrentUserId;
import com.jjap.berries.user.dto.UpdateUserRequest;
import com.jjap.berries.user.dto.UserResponse;
import com.jjap.berries.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService service;

  @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 계정과 프로필 정보를 조회합니다.")
  @GetMapping("/me")
  public ApiResponse<UserResponse> me(@CurrentUserId Long userId) {
    return ApiResponse.success(service.me(userId), "회원 정보입니다.");
  }

  @Operation(summary = "회원 정보 조회", description = "회원 식별자로 특정 회원의 정보를 조회합니다.")
  @GetMapping("/{userId}")
  public ApiResponse<UserResponse> get(@PathVariable Long userId) {
    return ApiResponse.success(service.get(userId), "회원 정보입니다.");
  }

  @Operation(summary = "내 정보 수정", description = "로그인한 사용자의 닉네임, 프로필 이미지와 비밀번호를 수정합니다.")
  @PatchMapping("/me")
  public ApiResponse<UserResponse> update(
      @CurrentUserId Long userId, @Valid @RequestBody UpdateUserRequest request) {
    return ApiResponse.success(service.update(userId, request), "회원 정보를 수정했습니다.");
  }

  @Operation(summary = "회원 탈퇴", description = "로그인한 사용자의 계정을 탈퇴 상태로 변경합니다.")
  @DeleteMapping("/me")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void withdraw(@CurrentUserId Long userId) {
    service.withdraw(userId);
  }
}
