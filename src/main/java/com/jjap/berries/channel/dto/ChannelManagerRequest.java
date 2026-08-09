package com.jjap.berries.channel.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ChannelManagerRequest(@NotNull @Positive Long managerId) {}
