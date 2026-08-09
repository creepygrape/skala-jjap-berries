package com.jjap.berries.channel.domain;

import com.jjap.berries.global.common.domain.BaseEntity;
import com.jjap.berries.user.domain.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "channel_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelUser extends BaseEntity {

  @ManyToOne(optional = false)
  @JoinColumn(name = "channel_id", nullable = false)
  private Channel channel;

  @OneToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  public ChannelUser(Channel channel, User user) {
    this.channel = channel;
    this.user = user;
  }
}
