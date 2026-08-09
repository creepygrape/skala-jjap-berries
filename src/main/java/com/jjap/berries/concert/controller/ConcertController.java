package com.jjap.berries.concert.controller;

import com.jjap.berries.concert.dto.ConcertCreateRequest;
import com.jjap.berries.concert.dto.ConcertResponse;
import com.jjap.berries.concert.dto.ConcertStatusRequest;
import com.jjap.berries.concert.dto.ConcertUpdateRequest;
import com.jjap.berries.concert.service.ConcertService;
import com.jjap.berries.global.common.response.ApiResponse;
import com.jjap.berries.global.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/concerts")
@RequiredArgsConstructor
public class ConcertController {
  private final ConcertService concerts;

  @Operation(summary = "공연 목록 조회", description = "채널의 공연 목록을 조회합니다.")
  @GetMapping
  public ApiResponse<List<ConcertResponse>> list(@RequestParam Long channelId) {
    return ApiResponse.success(concerts.list(channelId), "공연 목록입니다.");
  }

  @Operation(summary = "공연 상세 조회", description = "공연 ID로 장소, 공연 일시, 예매 기간과 판매 상태를 조회합니다.")
  @GetMapping("/{concertId}")
  public ApiResponse<ConcertResponse> get(@PathVariable Long concertId) {
    return ApiResponse.success(concerts.get(concertId), "공연 정보입니다.");
  }

  @Operation(summary = "공연 등록", description = "담당 채널 매니저가 미래 예매 기간과 공연 정보를 등록합니다. channelId는 필수입니다.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<ConcertResponse> create(
      @CurrentUserId Long userId,
      @RequestParam Long channelId,
      @Valid @RequestBody ConcertCreateRequest request) {
    return ApiResponse.success(concerts.create(userId, channelId, request), "공연을 등록했습니다.");
  }

  @Operation(summary = "공연 수정", description = "예매 시작 전에는 전체 정보를, 시작 후에는 제목과 장소만 수정할 수 있습니다.")
  @PatchMapping("/{concertId}")
  public ApiResponse<ConcertResponse> update(
      @CurrentUserId Long userId,
      @PathVariable Long concertId,
      @Valid @RequestBody ConcertUpdateRequest request) {
    return ApiResponse.success(concerts.update(userId, concertId, request), "공연을 수정했습니다.");
  }

  @Operation(summary = "공연 상태 변경", description = "담당 채널 매니저가 공연 상태를 ON_SALE 또는 STOPPED로 변경합니다.")
  @PatchMapping("/{concertId}/status")
  public ApiResponse<ConcertResponse> status(
      @CurrentUserId Long userId,
      @PathVariable Long concertId,
      @Valid @RequestBody ConcertStatusRequest request) {
    return ApiResponse.success(
        concerts.status(userId, concertId, request.status()), "공연 상태를 변경했습니다.");
  }
}
