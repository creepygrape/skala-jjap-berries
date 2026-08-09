package com.jjap.berries.auth.service;

import com.jjap.berries.auth.domain.RevokedAccessToken;
import com.jjap.berries.auth.repository.RevokedAccessTokenRepository;
import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RevokedAccessTokenService {

  private final RevokedAccessTokenRepository revokedTokens;

  public void revoke(String token, Claims claims) {
    LocalDateTime now = LocalDateTime.now();
    revokedTokens.deleteByExpiresAtBefore(now);
    String tokenHash = hash(token);
    if (!revokedTokens.existsByTokenHashAndExpiresAtAfter(tokenHash, now)) {
      LocalDateTime expiresAt =
          LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault());
      revokedTokens.save(new RevokedAccessToken(tokenHash, expiresAt));
    }
  }

  public boolean isRevoked(String token) {
    return revokedTokens.existsByTokenHashAndExpiresAtAfter(hash(token), LocalDateTime.now());
  }

  private String hash(String token) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
      return java.util.HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }
}
