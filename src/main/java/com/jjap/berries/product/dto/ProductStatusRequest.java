package com.jjap.berries.product.dto;

import com.jjap.berries.product.domain.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ProductStatusRequest(
    @Schema(example = "ON_SALE | STOPPED") @NotNull ProductStatus status) {}
