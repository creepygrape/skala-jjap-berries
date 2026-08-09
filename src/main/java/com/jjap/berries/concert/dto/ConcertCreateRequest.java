package com.jjap.berries.concert.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record ConcertCreateRequest(
    @NotBlank @Size(max = 200) String title,
    @NotBlank @Size(max = 300) String venue,
    @NotNull LocalDateTime concertAt,
    @NotNull LocalDateTime reservationStartAt,
    @NotNull LocalDateTime reservationEndAt) {}
