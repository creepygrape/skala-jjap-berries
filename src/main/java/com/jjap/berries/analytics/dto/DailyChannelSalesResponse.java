package com.jjap.berries.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyChannelSalesResponse(
    LocalDate date,
    long orderCount,
    long quantitySold,
    BigDecimal productSalesAmount,
    long reservationCount,
    BigDecimal concertSalesAmount,
    BigDecimal totalSalesAmount) {}
