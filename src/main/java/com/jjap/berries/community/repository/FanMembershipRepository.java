package com.jjap.berries.community.repository;

import com.jjap.berries.community.domain.FanMembership;
import com.jjap.berries.user.domain.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FanMembershipRepository extends JpaRepository<FanMembership, Long> {
  boolean existsByChannelIdAndUserId(Long channelId, Long userId);

  Optional<FanMembership> findByChannelIdAndUserId(Long channelId, Long userId);

  List<FanMembership> findAllByUserId(Long userId);

  List<FanMembership> findAllByChannelIdAndUserRoleOrderByCreatedAtDesc(
      Long channelId, UserRole role);
}
