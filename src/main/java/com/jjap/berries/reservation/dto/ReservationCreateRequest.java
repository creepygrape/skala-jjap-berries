package com.jjap.berries.reservation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationCreateRequest(@NotNull @Positive Long seatId) {}
