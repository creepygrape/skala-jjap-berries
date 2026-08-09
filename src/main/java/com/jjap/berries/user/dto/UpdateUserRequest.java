package com.jjap.berries.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @Size(min = 1, max = 30, message = "닉네임은 1자 이상 30자 이하여야 합니다.") String nickname,
    @Size(max = 500, message = "프로필 이미지 URL은 500자 이하여야 합니다.") String profileImageUrl,
    @Schema(example = "newPassword123!")
        @Size(min = 4, max = 64, message = "비밀번호는 4자 이상 64자 이하여야 합니다.")
        @Pattern(regexp = "^[\\x21-\\x7E]+$", message = "비밀번호에는 공백을 사용할 수 없습니다.")
        String password) {}
