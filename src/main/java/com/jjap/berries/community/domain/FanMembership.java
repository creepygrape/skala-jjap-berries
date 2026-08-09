package com.jjap.berries.community.domain;

import com.jjap.berries.channel.domain.Channel;
import com.jjap.berries.global.common.domain.BaseEntity;
import com.jjap.berries.user.domain.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"channel_id", "user_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FanMembership extends BaseEntity {

  @ManyToOne(optional = false)
  @JoinColumn(name = "channel_id", nullable = false)
  private Channel channel;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  public FanMembership(Channel channel, User user) {
    this.channel = channel;
    this.user = user;
  }
}
