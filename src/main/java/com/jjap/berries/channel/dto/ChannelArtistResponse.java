package com.jjap.berries.channel.dto;

import com.jjap.berries.user.domain.User;

public record ChannelArtistResponse(Long artistId, String nickname, String profileImageUrl) {
  public static ChannelArtistResponse from(User artist) {
    return new ChannelArtistResponse(
        artist.getId(), artist.getNickname(), artist.getProfileImageUrl());
  }
}
