package com.jjap.berries.order.dto;

import com.jjap.berries.order.domain.Order;
import com.jjap.berries.order.domain.OrderItem;
import com.jjap.berries.order.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long orderId,
    OrderStatus status,
    BigDecimal totalPrice,
    LocalDateTime createdAt,
    List<OrderItemResponse> items) {
  public static OrderResponse from(Order order, List<OrderItem> items) {
    return new OrderResponse(
        order.getId(),
        order.getStatus(),
        order.getTotalPrice(),
        order.getCreatedAt(),
        items.stream().map(OrderItemResponse::from).toList());
  }
}
