package com.jjap.berries.channel.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChannelUpdateRequest(
    @Size(max = 100) @Pattern(regexp = "(?s).*\\S.*") String name,
    @Size(max = 2000) @Pattern(regexp = "(?s).*\\S.*") String description,
    @Size(max = 500) String profileImageUrl) {}
