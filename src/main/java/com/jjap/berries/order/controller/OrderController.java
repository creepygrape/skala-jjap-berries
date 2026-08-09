package com.jjap.berries.order.controller;

import com.jjap.berries.global.common.response.ApiResponse;
import com.jjap.berries.global.security.CurrentUserId;
import com.jjap.berries.order.dto.OrderCreateRequest;
import com.jjap.berries.order.dto.OrderResponse;
import com.jjap.berries.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
  private final OrderService orders;

  @Operation(summary = "주문 생성", description = "판매 중인 상품의 재고를 확인하고 로그인한 사용자의 주문을 생성합니다.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<OrderResponse> create(
      @CurrentUserId Long userId, @Valid @RequestBody OrderCreateRequest request) {
    return ApiResponse.success(orders.create(userId, request), "주문을 생성했습니다.");
  }

  @Operation(summary = "내 주문 목록 조회", description = "로그인한 사용자의 주문 목록을 조회합니다.")
  @GetMapping
  public ApiResponse<List<OrderResponse>> list(@CurrentUserId Long userId) {
    return ApiResponse.success(orders.list(userId), "주문 목록입니다.");
  }

  @Operation(summary = "주문 상세 조회", description = "주문자 본인의 주문과 주문 상품 상세 정보를 조회합니다.")
  @GetMapping("/{orderId}")
  public ApiResponse<OrderResponse> get(
      @CurrentUserId Long userId, @PathVariable Long orderId) {
    return ApiResponse.success(orders.get(userId, orderId), "주문 정보입니다.");
  }

  @Operation(summary = "주문 취소", description = "취소 가능한 주문을 취소하고 주문 상품의 재고를 복구합니다.")
  @PostMapping("/{orderId}/cancel")
  public ApiResponse<OrderResponse> cancel(
      @CurrentUserId Long userId, @PathVariable Long orderId) {
    return ApiResponse.success(orders.cancel(userId, orderId), "주문을 취소했습니다.");
  }
}
