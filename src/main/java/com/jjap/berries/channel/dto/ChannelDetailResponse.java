package com.jjap.berries.channel.dto;

import com.jjap.berries.channel.domain.Channel;
import java.util.List;

public record ChannelDetailResponse(
    Long channelId,
    String name,
    String description,
    String profileImageUrl,
    List<ChannelArtistResponse> artists) {

  public static ChannelDetailResponse from(
      Channel channel, List<ChannelArtistResponse> artists) {
    return new ChannelDetailResponse(
        channel.getId(),
        channel.getName(),
        channel.getDescription(),
        channel.getProfileImageUrl(),
        artists);
  }
}
