package com.jjap.berries.channel.domain;

import com.jjap.berries.global.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "channel")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Channel extends BaseEntity {

  @Column(nullable = false, unique = true, length = 100)
  private String name;

  @Column(nullable = false, length = 2_000)
  private String description;

  @Column(length = 500)
  private String profileImageUrl;

  public Channel(String name, String description, String profileImageUrl) {
    this.name = name;
    this.description = description;
    this.profileImageUrl = profileImageUrl;
  }

  public void update(String name, String description, String profileImageUrl) {
    if (name != null) {
      this.name = name;
    }
    if (description != null) {
      this.description = description;
    }
    if (profileImageUrl != null) {
      this.profileImageUrl = profileImageUrl;
    }
  }
}
