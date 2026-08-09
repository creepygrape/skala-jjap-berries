package com.jjap.berries.community.controller;

import com.jjap.berries.community.dto.CommunityResponse;
import com.jjap.berries.community.service.CommunityService;
import com.jjap.berries.global.common.response.ApiResponse;
import com.jjap.berries.global.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/memberships")
@RequiredArgsConstructor
public class CommunityController {
  private final CommunityService service;

  @Operation(summary = "팬 커뮤니티 가입", description = "일반 사용자가 지정한 아티스트의 팬 커뮤니티에 가입합니다.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public void join(@CurrentUserId Long userId, @RequestParam Long channelId) {
    service.join(userId, channelId);
  }

  @Operation(summary = "팬 커뮤니티 탈퇴", description = "로그인한 사용자가 지정한 아티스트의 팬 커뮤니티에서 탈퇴합니다.")
  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void leave(@CurrentUserId Long userId, @RequestParam Long channelId) {
    service.leave(userId, channelId);
  }

  @Operation(summary = "내 커뮤니티 목록 조회", description = "로그인한 사용자가 가입한 아티스트 커뮤니티 목록을 조회합니다.")
  @GetMapping("/me")
  public ApiResponse<List<CommunityResponse>> mine(@CurrentUserId Long userId) {
    return ApiResponse.success(service.mine(userId), "가입 커뮤니티 목록입니다.");
  }
}
