package com.jjap.berries.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import com.jjap.berries.user.domain.UserRole;

public record SignupRequest(
    @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
        String email,
    @Schema(defaultValue = "1234", example = "1234")
    @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 4, max = 64, message = "비밀번호는 4자 이상 64자 이하여야 합니다.")
        @Pattern(regexp = "^[\\x21-\\x7E]+$", message = "비밀번호에는 공백을 사용할 수 없습니다.")
        String password,
    @NotBlank(message = "닉네임은 필수입니다.") @Size(max = 30, message = "닉네임은 30자 이하여야 합니다.")
        String nickname,
    @Schema(example = "USER | ARTIST | MANAGER")
        @NotNull(message = "역할은 필수입니다.")
        UserRole role) {}
