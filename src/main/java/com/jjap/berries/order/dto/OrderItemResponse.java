package com.jjap.berries.order.dto;

import com.jjap.berries.order.domain.OrderItem;
import java.math.BigDecimal;

public record OrderItemResponse(
    Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal subtotal) {
  public static OrderItemResponse from(OrderItem item) {
    return new OrderItemResponse(
        item.getProduct().getId(),
        item.getProduct().getName(),
        item.getUnitPrice(),
        item.getQuantity(),
        item.calculatePrice());
  }
}
