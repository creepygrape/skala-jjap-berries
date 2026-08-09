package com.jjap.berries.channel.dto;

import com.jjap.berries.channel.domain.Channel;

public record ChannelResponse(
    Long channelId, String name, String description, String profileImageUrl) {
  public static ChannelResponse from(Channel channel) {
    return new ChannelResponse(
        channel.getId(),
        channel.getName(),
        channel.getDescription(),
        channel.getProfileImageUrl());
  }
}
