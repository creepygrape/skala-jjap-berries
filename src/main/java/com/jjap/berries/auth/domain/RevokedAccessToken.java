package com.jjap.berries.auth.domain;

import com.jjap.berries.global.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "revoked_access_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RevokedAccessToken extends BaseEntity {

  @Column(nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  public RevokedAccessToken(String tokenHash, LocalDateTime expiresAt) {
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
  }
}
