package com.jjap.berries.auth.repository;

import com.jjap.berries.auth.domain.RevokedAccessToken;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedAccessTokenRepository extends JpaRepository<RevokedAccessToken, Long> {

  boolean existsByTokenHashAndExpiresAtAfter(String tokenHash, LocalDateTime now);

  void deleteByExpiresAtBefore(LocalDateTime now);
}
