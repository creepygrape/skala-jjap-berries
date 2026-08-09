package com.jjap.berries.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ChannelSalesReportResponse(
    Long channelId,
    String channelName,
    LocalDate from,
    LocalDate to,
    long totalOrderCount,
    long totalQuantitySold,
    BigDecimal totalProductSalesAmount,
    long totalReservationCount,
    BigDecimal totalConcertSalesAmount,
    BigDecimal totalSalesAmount,
    List<DailyChannelSalesResponse> dailySales) {}
