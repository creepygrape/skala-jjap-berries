package com.jjap.berries.analytics.controller;

import com.jjap.berries.analytics.dto.ChannelSalesReportResponse;
import com.jjap.berries.analytics.dto.ProductSalesResponse;
import com.jjap.berries.analytics.dto.ConcertSalesResponse;
import com.jjap.berries.analytics.dto.SeatGradeSalesResponse;
import com.jjap.berries.analytics.service.ChannelSalesService;
import com.jjap.berries.global.common.response.ApiResponse;
import com.jjap.berries.global.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class ChannelSalesController {

  private final ChannelSalesService salesService;

  @Operation(
      summary = "채널 기간별 매출 통계 조회",
      description = "담당 채널의 지정 기간 전체 및 일별 주문 건수, 판매 수량과 매출액을 조회합니다.")
  @GetMapping("/sales")
  public ApiResponse<ChannelSalesReportResponse> get(
      @CurrentUserId Long userId,
      @RequestParam Long channelId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return ApiResponse.success(
        salesService.get(userId, channelId, from, to), "채널 기간별 매출 통계입니다.");
  }
  @Operation(summary = "상품별 매출 조회", description = "담당 채널의 상품별 주문 건수, 판매 수량과 매출을 조회합니다.")
  @GetMapping("/sales/products")
  public ApiResponse<List<ProductSalesResponse>> products(
      @CurrentUserId Long userId,
      @RequestParam Long channelId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return ApiResponse.success(
        salesService.getProductSales(userId, channelId, from, to), "상품별 매출입니다.");
  }

  @Operation(summary = "공연별 매출 조회", description = "담당 채널의 공연별 예매 건수와 매출을 조회합니다.")
  @GetMapping("/sales/concerts")
  public ApiResponse<List<ConcertSalesResponse>> concerts(
      @CurrentUserId Long userId,
      @RequestParam Long channelId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return ApiResponse.success(
        salesService.getConcertSales(userId, channelId, from, to), "공연별 매출입니다.");
  }

  @Operation(summary = "좌석 등급별 판매율 조회", description = "공연의 좌석 등급별 예매 좌석 수, 판매율과 매출을 조회합니다.")
  @GetMapping("/seat-grades")
  public ApiResponse<List<SeatGradeSalesResponse>> seatGrades(
      @CurrentUserId Long userId,
      @RequestParam Long channelId,
      @RequestParam Long concertId) {
    return ApiResponse.success(
        salesService.getSeatGradeSales(userId, channelId, concertId), "좌석 등급별 판매 현황입니다.");
  }
}
