package com.jjap.berries.analytics.dto;

import java.math.BigDecimal;

public record ProductSalesResponse(
    Long productId,
    String productName,
    long orderCount,
    long quantitySold,
    BigDecimal salesAmount) {}
