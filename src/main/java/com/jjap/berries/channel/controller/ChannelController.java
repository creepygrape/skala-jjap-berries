package com.jjap.berries.channel.controller;

import com.jjap.berries.channel.dto.ChannelCreateRequest;
import com.jjap.berries.channel.dto.ChannelDetailResponse;
import com.jjap.berries.channel.dto.ChannelFanResponse;
import com.jjap.berries.channel.dto.ChannelManagerRequest;
import com.jjap.berries.channel.dto.ChannelMemberRequest;
import com.jjap.berries.channel.dto.ChannelResponse;
import com.jjap.berries.channel.dto.ChannelUpdateRequest;
import com.jjap.berries.channel.service.ChannelService;
import com.jjap.berries.global.common.response.ApiResponse;
import com.jjap.berries.global.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {
  private final ChannelService service;

  @Operation(summary = "채널 목록 조회", description = "등록된 채널 목록을 페이지 단위로 조회합니다.")
  @GetMapping
  public ApiResponse<Page<ChannelResponse>> list(
      @ParameterObject @PageableDefault Pageable pageable) {
    return ApiResponse.success(service.list(pageable), "채널 목록입니다.");
  }

  @Operation(summary = "채널 상세 조회", description = "채널 식별자로 상세 정보를 조회합니다.")
  @GetMapping("/{channelId}")
  public ApiResponse<ChannelDetailResponse> get(@PathVariable Long channelId) {
    return ApiResponse.success(service.get(channelId), "채널 정보입니다.");
  }

  @Operation(summary = "채널 등록", description = "MANAGER가 새 채널을 등록하고 담당 매니저로 자동 지정됩니다.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<ChannelResponse> create(
      @CurrentUserId Long currentManagerId,
      @Valid @RequestBody ChannelCreateRequest request) {
    return ApiResponse.success(
        service.create(currentManagerId, request), "채널을 등록했습니다.");
  }

  @Operation(summary = "채널 수정", description = "담당 매니저가 채널의 이름, 소개와 프로필 이미지를 수정합니다.")
  @PatchMapping("/{channelId}")
  public ApiResponse<ChannelResponse> update(
      @CurrentUserId Long currentManagerId,
      @PathVariable Long channelId,
      @Valid @RequestBody ChannelUpdateRequest request) {
    return ApiResponse.success(
        service.update(currentManagerId, channelId, request), "채널을 수정했습니다.");
  }

  @Operation(
      summary = "채널 멤버 추가",
      description = "매니저가 채널 ID와 ARTIST 역할 회원 ID를 입력해 채널 멤버로 연결합니다.")
  @PostMapping("/{channelId}/members")
  @ResponseStatus(HttpStatus.CREATED)
  public void member(
      @CurrentUserId Long currentManagerId,
      @PathVariable Long channelId,
      @Valid @RequestBody ChannelMemberRequest request) {
    service.addMember(currentManagerId, channelId, request.artistId());
  }

  @Operation(summary = "채널 멤버 삭제", description = "담당 매니저가 채널과 아티스트 사용자의 연결을 해제합니다.")
  @DeleteMapping("/{channelId}/members")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unmember(
      @CurrentUserId Long currentManagerId,
      @PathVariable Long channelId,
      @Valid @RequestBody ChannelMemberRequest request) {
    service.removeMember(currentManagerId, channelId, request.artistId());
  }

  @Operation(summary = "채널 매니저 지정", description = "담당 매니저가 다른 MANAGER 사용자를 해당 채널의 매니저로 지정합니다.")
  @PostMapping("/{channelId}/managers")
  @ResponseStatus(HttpStatus.CREATED)
  public void manager(
      @CurrentUserId Long currentManagerId,
      @PathVariable Long channelId,
      @Valid @RequestBody ChannelManagerRequest request) {
    service.addManager(currentManagerId, channelId, request.managerId());
  }

  @Operation(summary = "채널 매니저 해제", description = "담당 매니저가 대상 사용자의 채널 관리 권한을 해제합니다.")
  @DeleteMapping("/{channelId}/managers/{managerId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unmanager(
      @CurrentUserId Long currentManagerId,
      @PathVariable Long channelId,
      @PathVariable Long managerId) {
    service.removeManager(currentManagerId, channelId, managerId);
  }

  @Operation(
      summary = "담당 채널 가입 사용자 조회",
      description = "매니저가 자신이 담당하는 채널에 가입한 일반 사용자 목록을 조회합니다.")
  @GetMapping("/{channelId}/members")
  public ApiResponse<List<ChannelFanResponse>> fans(
      @CurrentUserId Long currentManagerId, @PathVariable Long channelId) {
    return ApiResponse.success(
        service.fans(currentManagerId, channelId), "채널 가입 사용자 목록입니다.");
  }

  @Operation(summary = "담당 채널 조회", description = "로그인한 매니저가 담당하는 채널 목록을 조회합니다.")
  @GetMapping("/managed")
  public ApiResponse<List<ChannelResponse>> managed(@CurrentUserId Long currentManagerId) {
    return ApiResponse.success(service.managed(currentManagerId), "담당 채널 목록입니다.");
  }
}
