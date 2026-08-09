package com.jjap.berries.post.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
    @Size(max = 200) String title,
    @Size(max = 10000) @Pattern(regexp = "(?s).*\\S.*") String content) {}
