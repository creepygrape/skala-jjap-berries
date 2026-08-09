package com.jjap.berries.concert.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import java.math.BigDecimal;

public record SeatCreateRequest(
    @NotBlank @Size(max = 50) String section,
    @NotNull @Positive @Max(1_000_000) Integer seatSequence,
    @NotBlank @Size(max = 50) String seatLabel,
    @NotBlank @Size(max = 50) String grade,
    @NotNull @PositiveOrZero BigDecimal price) {}
