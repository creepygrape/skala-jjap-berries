package com.jjap.berries.concert.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record SeatBulkRequest(
    @NotBlank @Size(max = 50) String section,
    @Positive @Max(1_000_000) Integer startNumber,
    @NotNull @Positive @Max(500) Integer count,
    @NotBlank @Size(max = 50) String grade,
    @NotNull @PositiveOrZero BigDecimal price) {}
