package com.jjap.berries.channel.service;

import com.jjap.berries.channel.domain.Channel;
import com.jjap.berries.channel.domain.ChannelUser;
import com.jjap.berries.channel.dto.ChannelCreateRequest;
import com.jjap.berries.channel.dto.ChannelArtistResponse;
import com.jjap.berries.channel.dto.ChannelDetailResponse;
import com.jjap.berries.channel.dto.ChannelFanResponse;
import com.jjap.berries.channel.dto.ChannelResponse;
import com.jjap.berries.channel.dto.ChannelUpdateRequest;
import com.jjap.berries.channel.repository.ChannelRepository;
import com.jjap.berries.channel.repository.ChannelUserRepository;
import com.jjap.berries.community.repository.FanMembershipRepository;
import com.jjap.berries.global.exception.BusinessException;
import com.jjap.berries.global.exception.ErrorCode;
import com.jjap.berries.global.service.AccessService;
import com.jjap.berries.user.domain.User;
import com.jjap.berries.user.domain.UserRole;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChannelService {
  private final ChannelRepository channels;
  private final ChannelUserRepository channelUsers;
  private final FanMembershipRepository memberships;
  private final AccessService access;

  public Page<ChannelResponse> list(Pageable pageable) {
    return channels.findAll(pageable).map(ChannelResponse::from);
  }

  public ChannelDetailResponse get(Long id) {
    Channel channel = channel(id);
    List<ChannelArtistResponse> artists =
        channelUsers.findAllByChannelIdAndUserRole(id, UserRole.ARTIST).stream()
            .map(relation -> ChannelArtistResponse.from(relation.getUser()))
            .toList();
    return ChannelDetailResponse.from(channel, artists);
  }

  @Transactional
  public ChannelResponse create(Long userId, ChannelCreateRequest request) {
    User manager = access.user(userId);
    access.manager(manager);
    if (channelUsers.existsByUserId(userId)) {
      throw new BusinessException(ErrorCode.CHANNEL_USER_ALREADY_EXISTS);
    }
    Channel channel =
        channels.save(new Channel(request.name(), request.description(), request.profileImageUrl()));
    channelUsers.save(new ChannelUser(channel, manager));
    return ChannelResponse.from(channel);
  }

  @Transactional
  public ChannelResponse update(Long userId, Long id, ChannelUpdateRequest request) {
    access.manager(access.user(userId), id);
    Channel channel = channel(id);
    channel.update(request.name(), request.description(), request.profileImageUrl());
    return ChannelResponse.from(channel);
  }

  @Transactional
  public void addMember(Long currentManagerId, Long channelId, Long artistId) {
    Channel channel = channel(channelId);
    access.manager(access.user(currentManagerId), channelId);
    User artistUser = access.user(artistId);
    if (artistUser.getRole() != UserRole.ARTIST) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
    channelUsers
        .findByUserId(artistId)
        .ifPresent(
            relation -> {
              ErrorCode error =
                  relation.getChannel().getId().equals(channelId)
                      ? ErrorCode.SAME_CHANNEL_USER_ALREADY_EXISTS
                      : ErrorCode.CHANNEL_USER_ALREADY_EXISTS;
              throw new BusinessException(error);
            });
    channelUsers.save(new ChannelUser(channel, artistUser));
  }

  @Transactional
  public void removeMember(Long currentManagerId, Long channelId, Long artistId) {
    access.manager(access.user(currentManagerId), channelId);
    if (access.user(artistId).getRole() != UserRole.ARTIST) {
      throw new BusinessException(ErrorCode.NOT_ARTIST_MEMBER);
    }
    channelUsers
        .findByChannelIdAndUserId(channelId, artistId)
        .ifPresentOrElse(
            channelUsers::delete,
            () -> {
              throw new BusinessException(ErrorCode.NOT_ARTIST_MEMBER);
            });
  }

  @Transactional
  public void addManager(Long currentManagerId, Long channelId, Long managerId) {
    access.manager(access.user(currentManagerId), channelId);
    User manager = access.user(managerId);
    if (manager.getRole() != UserRole.MANAGER) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
    if (channelUsers.existsByUserId(managerId)) {
      throw new BusinessException(ErrorCode.CHANNEL_USER_ALREADY_EXISTS);
    }
    channelUsers.save(new ChannelUser(channel(channelId), manager));
  }

  @Transactional
  public void removeManager(Long currentManagerId, Long channelId, Long managerId) {
    access.manager(access.user(currentManagerId), channelId);
    if (currentManagerId.equals(managerId)) {
      throw new BusinessException(ErrorCode.CANNOT_REMOVE_SELF);
    }
    if (access.user(managerId).getRole() != UserRole.MANAGER) {
      throw new BusinessException(ErrorCode.NOT_MANAGER);
    }
    channelUsers
        .findByChannelIdAndUserId(channelId, managerId)
        .ifPresentOrElse(
            channelUsers::delete,
            () -> {
              throw new BusinessException(ErrorCode.NOT_MANAGER);
            });
  }

  public List<ChannelResponse> managed(Long userId) {
    User user = access.user(userId);
    if (user.getRole() != UserRole.MANAGER) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
    return channelUsers.findAllByUserId(userId).stream()
        .map(relation -> ChannelResponse.from(relation.getChannel()))
        .toList();
  }

  public List<ChannelFanResponse> fans(Long managerId, Long channelId) {
    channel(channelId);
    access.manager(access.user(managerId), channelId);
    return memberships
        .findAllByChannelIdAndUserRoleOrderByCreatedAtDesc(channelId, UserRole.USER)
        .stream()
        .map(ChannelFanResponse::from)
        .toList();
  }

  private Channel channel(Long id) {
    return channels
        .findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));
  }
}
