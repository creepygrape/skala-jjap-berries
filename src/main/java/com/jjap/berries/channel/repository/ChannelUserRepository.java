package com.jjap.berries.channel.repository;

import com.jjap.berries.channel.domain.ChannelUser;
import com.jjap.berries.user.domain.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelUserRepository extends JpaRepository<ChannelUser, Long> {
  boolean existsByChannelIdAndUserId(Long channelId, Long userId);

  boolean existsByUserId(Long userId);

  Optional<ChannelUser> findByChannelIdAndUserId(Long channelId, Long userId);

  Optional<ChannelUser> findByUserId(Long userId);

  List<ChannelUser> findAllByUserId(Long userId);

  List<ChannelUser> findAllByChannelIdAndUserRole(Long channelId, UserRole role);
}
