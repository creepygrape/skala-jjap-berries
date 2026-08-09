package com.jjap.berries.concert.dto;

import com.jjap.berries.concert.domain.SeatStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import java.math.BigDecimal;

public record SeatUpdateRequest(
    @Size(max = 50) @Pattern(regexp = "(?s).*\\S.*") String section,
    @Positive @Max(1_000_000) Integer seatSequence,
    @Size(max = 50) @Pattern(regexp = "(?s).*\\S.*") String seatLabel,
    @Size(max = 50) @Pattern(regexp = "(?s).*\\S.*") String grade,
    @PositiveOrZero BigDecimal price,
    SeatStatus status) {}
