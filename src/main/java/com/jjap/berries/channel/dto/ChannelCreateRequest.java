package com.jjap.berries.channel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChannelCreateRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Size(max = 2000) String description,
    @Size(max = 500) String profileImageUrl) {}
