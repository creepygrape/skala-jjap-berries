package com.jjap.berries.concert.controller;

import com.jjap.berries.concert.domain.SeatStatus;
import com.jjap.berries.concert.dto.SeatBulkCreateResponse;
import com.jjap.berries.concert.dto.SeatBulkRequest;
import com.jjap.berries.concert.dto.SeatCreateRequest;
import com.jjap.berries.concert.dto.SeatResponse;
import com.jjap.berries.concert.dto.SeatPageResponse;
import com.jjap.berries.concert.dto.SeatUpdateRequest;
import com.jjap.berries.concert.service.SeatService;
import com.jjap.berries.global.common.response.ApiResponse;
import com.jjap.berries.global.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/seats")
@RequiredArgsConstructor
@Validated
public class SeatController {
  private final SeatService seats;

  @Operation(summary = "좌석 목록 조회", description = "concertId는 필수이며 구역·등급·상태 필터와 페이지 정보를 함께 반환합니다.")
  @GetMapping
  public ApiResponse<SeatPageResponse> list(
      @RequestParam Long concertId,
      @RequestParam(required = false) @Size(max = 50) String section,
      @RequestParam(required = false) @Size(max = 50) String grade,
      @RequestParam(required = false) SeatStatus status,
      @RequestParam(defaultValue = "0") @Min(0) @Max(1_000_000) int page,
      @RequestParam(defaultValue = "100") @Min(1) @Max(500) int size) {
    return ApiResponse.success(
        seats.list(concertId, section, grade, status, page, size), "좌석 목록입니다.");
  }

  @Operation(summary = "좌석 등록", description = "담당 매니저가 예매 시작 전 공연에 좌석 한 개를 등록합니다.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<SeatResponse> create(
      @CurrentUserId Long userId,
      @RequestParam Long concertId,
      @Valid @RequestBody SeatCreateRequest request) {
    return ApiResponse.success(seats.create(userId, concertId, request), "좌석을 등록했습니다.");
  }

  @Operation(summary = "좌석 일괄 등록", description = "startNumber 생략 시 같은 구역의 최대 순번 다음부터 최대 500석을 생성합니다.")
  @PostMapping("/bulk")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<SeatBulkCreateResponse> bulkCreate(
      @CurrentUserId Long userId,
      @RequestParam Long concertId,
      @Valid @RequestBody SeatBulkRequest request) {
    return ApiResponse.success(
        seats.bulkCreate(userId, concertId, request), "좌석을 일괄 등록했습니다.");
  }

  @Operation(summary = "좌석 수정", description = "예매 시작 전 좌석 순번, 표시명, 등급과 가격을 수정합니다.")
  @PatchMapping("/{seatId}")
  public ApiResponse<SeatResponse> update(
      @CurrentUserId Long userId,
      @PathVariable Long seatId,
      @Valid @RequestBody SeatUpdateRequest request) {
    return ApiResponse.success(seats.update(userId, seatId, request), "좌석을 수정했습니다.");
  }

  @Operation(summary = "좌석 삭제", description = "예매 시작 전이며 예매 이력이 없는 AVAILABLE 좌석만 삭제합니다.")
  @DeleteMapping("/{seatId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@CurrentUserId Long userId, @PathVariable Long seatId) {
    seats.delete(userId, seatId);
  }

}
