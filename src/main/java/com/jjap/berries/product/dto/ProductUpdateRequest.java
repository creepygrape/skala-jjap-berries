package com.jjap.berries.product.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductUpdateRequest(
    @Size(max = 200) @Pattern(regexp = "(?s).*\\S.*") String name,
    @Size(max = 2000) @Pattern(regexp = "(?s).*\\S.*") String description,
    @PositiveOrZero BigDecimal price,
    @PositiveOrZero Integer stock,
    @Size(max = 500) String imageUrl) {}
