package com.jjap.berries.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductCreateRequest(
    @NotBlank @Size(max = 200) String name,
    @NotBlank @Size(max = 2000) String description,
    @NotNull @PositiveOrZero BigDecimal price,
    @NotNull @PositiveOrZero Integer stock,
    @Size(max = 500) String imageUrl) {}
