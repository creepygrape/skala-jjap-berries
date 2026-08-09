package com.jjap.berries.product.controller;

import com.jjap.berries.global.common.response.ApiResponse;
import com.jjap.berries.global.security.CurrentUserId;
import com.jjap.berries.product.domain.ProductStatus;
import com.jjap.berries.product.dto.ProductCreateRequest;
import com.jjap.berries.product.dto.ProductResponse;
import com.jjap.berries.product.dto.ProductStatusRequest;
import com.jjap.berries.product.dto.ProductUpdateRequest;
import com.jjap.berries.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
  private final ProductService products;

  @Operation(summary = "채널 상품 목록 조회", description = "아티스트의 상품을 판매 상태와 페이지 조건으로 조회합니다.")
  @GetMapping
  public ApiResponse<Page<ProductResponse>> list(
      @RequestParam Long channelId,
      @RequestParam(required = false) ProductStatus status,
      @ParameterObject @PageableDefault Pageable pageable) {
    return ApiResponse.success(products.list(channelId, status, pageable), "상품 목록입니다.");
  }

  @Operation(summary = "상품 상세 조회", description = "상품 식별자로 가격, 재고, 판매 상태 등 상세 정보를 조회합니다.")
  @GetMapping("/{productId}")
  public ApiResponse<ProductResponse> get(@PathVariable Long productId) {
    return ApiResponse.success(products.get(productId), "상품 정보입니다.");
  }

  @Operation(summary = "상품 등록", description = "담당 매니저가 아티스트의 새 굿즈 상품을 등록합니다.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<ProductResponse> create(
      @CurrentUserId Long userId,
      @RequestParam Long channelId,
      @Valid @RequestBody ProductCreateRequest request) {
    return ApiResponse.success(products.create(userId, channelId, request), "상품을 등록했습니다.");
  }

  @Operation(summary = "상품 수정", description = "담당 매니저가 상품 정보와 재고를 수정합니다.")
  @PatchMapping("/{productId}")
  public ApiResponse<ProductResponse> update(
      @CurrentUserId Long userId,
      @PathVariable Long productId,
      @Valid @RequestBody ProductUpdateRequest request) {
    return ApiResponse.success(
        products.update(userId, productId, request), "상품을 수정했습니다.");
  }

  @Operation(summary = "상품 판매 상태 변경", description = "담당 매니저가 상품의 판매 상태를 변경합니다.")
  @PatchMapping("/{productId}/status")
  public ApiResponse<ProductResponse> status(
      @CurrentUserId Long userId,
      @PathVariable Long productId,
      @Valid @RequestBody ProductStatusRequest request) {
    return ApiResponse.success(
        products.status(userId, productId, request.status()), "상품 상태를 변경했습니다.");
  }
}
