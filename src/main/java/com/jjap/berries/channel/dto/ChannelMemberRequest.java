package com.jjap.berries.channel.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ChannelMemberRequest(@NotNull @Positive Long artistId) {}
