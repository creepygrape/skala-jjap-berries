package com.jjap.berries.product.dto;

import com.jjap.berries.product.domain.Product;
import com.jjap.berries.product.domain.ProductStatus;
import java.math.BigDecimal;

public record ProductResponse(
    Long productId,
    Long channelId,
    String name,
    String description,
    BigDecimal price,
    int stock,
    String imageUrl,
    ProductStatus status) {
  public static ProductResponse from(Product product) {
    return new ProductResponse(
        product.getId(),
        product.getChannel().getId(),
        product.getName(),
        product.getDescription(),
        product.getPrice(),
        product.getStock(),
        product.getImageUrl(),
        product.getStatus());
  }
}
