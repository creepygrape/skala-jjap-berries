package com.jjap.berries.concert.dto;

import com.jjap.berries.concert.domain.ConcertStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ConcertStatusRequest(
    @Schema(example = "ON_SALE | STOPPED") @NotNull ConcertStatus status) {}
