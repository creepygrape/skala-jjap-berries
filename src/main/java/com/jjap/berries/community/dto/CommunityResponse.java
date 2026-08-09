package com.jjap.berries.community.dto;

import com.jjap.berries.channel.domain.Channel;

public record CommunityResponse(Long channelId, String channelName, String profileImageUrl) {
  public static CommunityResponse from(Channel channel) {
    return new CommunityResponse(channel.getId(), channel.getName(), channel.getProfileImageUrl());
  }
}
