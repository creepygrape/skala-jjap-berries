package com.jjap.berries.user.domain;

import com.jjap.berries.global.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

  @Column(nullable = false, unique = true, length = 100)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, unique = true, length = 30)
  private String nickname;

  @Column(length = 500)
  private String profileImageUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private UserRole role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private UserStatus status;

  private LocalDateTime deletedAt;

  public User(String email, String password, String nickname) {
    this(email, password, nickname, UserRole.USER);
  }

  public User(String email, String password, String nickname, UserRole role) {
    this.email = email;
    this.password = password;
    this.nickname = nickname;
    this.role = role;
    this.status = UserStatus.ACTIVE;
  }

  public void withdraw() {
    this.status = UserStatus.WITHDRAWN;
    this.deletedAt = LocalDateTime.now();
  }

  public void changeRole(UserRole role) {
    this.role = role;
  }

  public void updateProfile(String nickname, String profileImageUrl) {
    if (nickname != null) {
      this.nickname = nickname;
    }
    if (profileImageUrl != null) {
      this.profileImageUrl = profileImageUrl;
    }
  }

  public void changePassword(String password) {
    this.password = password;
  }

  public void changeStatus(UserStatus status) {
    this.status = status;
    this.deletedAt = status == UserStatus.WITHDRAWN ? LocalDateTime.now() : null;
  }
}
