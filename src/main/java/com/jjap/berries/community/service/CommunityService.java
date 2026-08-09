package com.jjap.berries.community.service;

import com.jjap.berries.channel.domain.Channel;
import com.jjap.berries.channel.repository.ChannelRepository;
import com.jjap.berries.community.domain.FanMembership;
import com.jjap.berries.community.dto.CommunityResponse;
import com.jjap.berries.community.repository.FanMembershipRepository;
import com.jjap.berries.global.exception.BusinessException;
import com.jjap.berries.global.exception.ErrorCode;
import com.jjap.berries.global.service.AccessService;
import com.jjap.berries.user.domain.UserRole;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {
  private final FanMembershipRepository memberships;
  private final ChannelRepository channels;
  private final AccessService access;

  @Transactional
  public void join(Long userId, Long channelId) {
    var user = access.user(userId);
    if (user.getRole() != UserRole.USER) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
    if (memberships.existsByChannelIdAndUserId(channelId, userId)) {
      throw new BusinessException(ErrorCode.MEMBERSHIP_ALREADY_EXISTS);
    }
    memberships.save(new FanMembership(channel(channelId), user));
  }

  @Transactional
  public void leave(Long userId, Long channelId) {
    memberships
        .findByChannelIdAndUserId(channelId, userId)
        .ifPresentOrElse(
            memberships::delete,
            () -> {
              throw new BusinessException(ErrorCode.NOT_COMMUNITY_MEMBER);
            });
  }

  public List<CommunityResponse> mine(Long userId) {
    access.user(userId);
    return memberships.findAllByUserId(userId).stream()
        .map(m -> CommunityResponse.from(m.getChannel()))
        .toList();
  }

  private Channel channel(Long id) {
    return channels
        .findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));
  }
}
