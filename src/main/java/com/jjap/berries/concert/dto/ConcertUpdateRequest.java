package com.jjap.berries.concert.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record ConcertUpdateRequest(
    @Size(max = 200) @Pattern(regexp = "(?s).*\\S.*") String title,
    @Size(max = 300) @Pattern(regexp = "(?s).*\\S.*") String venue,
    LocalDateTime concertAt,
    LocalDateTime reservationStartAt,
    LocalDateTime reservationEndAt) {}
