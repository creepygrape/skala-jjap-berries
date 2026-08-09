package com.jjap.berries.auth.repository;

import com.jjap.berries.auth.domain.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByToken(String token);

  Optional<RefreshToken> findByUserId(Long userId);

  void deleteByUserId(Long userId);

}
