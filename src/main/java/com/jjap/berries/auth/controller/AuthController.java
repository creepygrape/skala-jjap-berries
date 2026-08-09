package com.jjap.berries.auth.controller;

import com.jjap.berries.auth.dto.LoginRequest;
import com.jjap.berries.auth.dto.SignupRequest;
import com.jjap.berries.auth.dto.SignupResponse;
import com.jjap.berries.auth.dto.TokenRequest;
import com.jjap.berries.auth.dto.TokenResponse;
import com.jjap.berries.auth.service.AuthService;
import com.jjap.berries.global.common.response.ApiResponse;
import com.jjap.berries.global.security.CurrentAccessToken;
import com.jjap.berries.global.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임과 역할을 입력해 새 계정을 생성합니다.")
  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<SignupResponse>> signup(
      @Valid @RequestBody SignupRequest request) {
    SignupResponse response = authService.signup(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(response, "회원가입에 성공했습니다."));
  }

  @Operation(summary = "로그인", description = "이메일과 비밀번호를 검증하고 Access Token과 Refresh Token을 발급합니다.")
  @PostMapping("/login")
  public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    return ApiResponse.success(authService.login(request), "로그인했습니다.");
  }

  @Operation(summary = "토큰 재발급", description = "유효한 Refresh Token으로 Access Token과 Refresh Token을 다시 발급합니다.")
  @PostMapping("/refresh")
  public ApiResponse<TokenResponse> refresh(@Valid @RequestBody TokenRequest request) {
    return ApiResponse.success(authService.refresh(request), "토큰을 재발급했습니다.");
  }

  @Operation(
      summary = "로그아웃",
      description =
          "현재 Access Token을 즉시 무효화하고 저장된 Refresh Token을 삭제해 재사용과 재발급을 차단합니다.")
  @PostMapping("/logout")
  public ApiResponse<Void> logout(
      @CurrentUserId Long userId,
      @Parameter(hidden = true) @CurrentAccessToken String accessToken) {
    authService.logout(userId, accessToken);
    return ApiResponse.success(null, "로그아웃했습니다.");
  }
}
