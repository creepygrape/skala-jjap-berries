package com.jjap.berries.analytics.dto;

import java.math.BigDecimal;

public record ConcertSalesResponse(
    Long concertId, String concertTitle, long reservationCount, BigDecimal salesAmount) {}
