package com.jjap.berries.channel.dto;

import com.jjap.berries.community.domain.FanMembership;
import com.jjap.berries.user.domain.UserStatus;
import java.time.LocalDateTime;

public record ChannelFanResponse(
    Long userId,
    String email,
    String nickname,
    String profileImageUrl,
    UserStatus status,
    LocalDateTime joinedAt) {

  public static ChannelFanResponse from(FanMembership membership) {
    var user = membership.getUser();
    return new ChannelFanResponse(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        user.getProfileImageUrl(),
        user.getStatus(),
        membership.getCreatedAt());
  }
}
