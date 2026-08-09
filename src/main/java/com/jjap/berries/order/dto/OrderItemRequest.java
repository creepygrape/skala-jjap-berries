package com.jjap.berries.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(@NotNull @Positive Long productId, @Positive int quantity) {}
