package com.jjap.berries.analytics.dto;

import java.math.BigDecimal;

public record SeatGradeSalesResponse(
    String grade,
    long totalSeatCount,
    long reservedSeatCount,
    BigDecimal reservationRate,
    BigDecimal salesAmount) {}
