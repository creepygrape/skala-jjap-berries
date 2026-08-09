package com.jjap.berries.reservation.controller;

import com.jjap.berries.global.common.response.ApiResponse;
import com.jjap.berries.global.security.CurrentUserId;
import com.jjap.berries.reservation.dto.ReservationCreateRequest;
import com.jjap.berries.reservation.dto.ReservationResponse;
import com.jjap.berries.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
  private final ReservationService service;

  @Operation(summary = "공연 좌석 예매", description = "예매 기간과 좌석 상태를 확인하고 선택한 공연 좌석을 예매합니다.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<ReservationResponse> create(
      @CurrentUserId Long userId,
      @RequestParam Long concertId,
      @Valid @RequestBody ReservationCreateRequest r) {
    return ApiResponse.success(service.create(userId, concertId, r), "좌석 예매가 완료되었습니다.");
  }

  @Operation(summary = "내 예매 목록 조회", description = "로그인한 사용자의 공연 좌석 예매 목록을 조회합니다.")
  @GetMapping
  public ApiResponse<List<ReservationResponse>> list(@CurrentUserId Long userId) {
    return ApiResponse.success(service.list(userId), "예매 목록입니다.");
  }

  @Operation(summary = "예매 상세 조회", description = "예약자 본인의 공연 및 좌석 예매 상세 정보를 조회합니다.")
  @GetMapping("/{reservationId}")
  public ApiResponse<ReservationResponse> get(
      @CurrentUserId Long userId, @PathVariable Long reservationId) {
    return ApiResponse.success(service.get(userId, reservationId), "예매 정보입니다.");
  }

  @Operation(summary = "예매 취소", description = "취소 가능한 예매를 취소하고 좌석을 다시 예매 가능한 상태로 변경합니다.")
  @PostMapping("/{reservationId}/cancel")
  public ApiResponse<ReservationResponse> cancel(
      @CurrentUserId Long userId, @PathVariable Long reservationId) {
    return ApiResponse.success(service.cancel(userId, reservationId), "예매를 취소했습니다.");
  }
}
