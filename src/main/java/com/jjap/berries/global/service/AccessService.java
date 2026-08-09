package com.jjap.berries.global.service;

import com.jjap.berries.channel.repository.ChannelUserRepository;
import com.jjap.berries.global.exception.BusinessException;
import com.jjap.berries.global.exception.ErrorCode;
import com.jjap.berries.user.domain.User;
import com.jjap.berries.user.domain.UserRole;
import com.jjap.berries.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessService {
  private final UserRepository users;
  private final ChannelUserRepository channelUsers;

  public User user(Long id) {
    return users.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }

  public void manager(User user) {
    if (user.getRole() != UserRole.MANAGER) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
  }

  public void manager(User user, Long channelId) {
    if (user.getRole() != UserRole.MANAGER
        || !channelUsers.existsByChannelIdAndUserId(channelId, user.getId()))
      throw new BusinessException(ErrorCode.NOT_MANAGER);
  }

  public boolean artistMember(User user, Long channelId) {
    return user.getRole() == UserRole.ARTIST
        && channelUsers.existsByChannelIdAndUserId(channelId, user.getId());
  }
}
