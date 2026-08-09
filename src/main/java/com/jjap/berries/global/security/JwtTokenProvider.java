package com.jjap.berries.global.security;

import com.jjap.berries.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
  private final SecretKey key;
  private final Duration accessExpiration;
  private final Duration refreshExpiration;

  public JwtTokenProvider(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-token-expiration}") Duration accessExpiration,
      @Value("${jwt.refresh-token-expiration}") Duration refreshExpiration) {
    this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    this.accessExpiration = accessExpiration;
    this.refreshExpiration = refreshExpiration;
  }

  public String createAccessToken(User user) {
    return create(user, "access", accessExpiration);
  }

  public String createRefreshToken(User user) {
    return create(user, "refresh", refreshExpiration);
  }

  private String create(User user, String type, Duration duration) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(user.getId().toString())
        .claim("role", user.getRole().name())
        .claim("type", type)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(duration)))
        .signWith(key)
        .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }

  public Long userId(Claims claims) {
    return Long.valueOf(claims.getSubject());
  }

  public boolean isType(Claims claims, String type) {
    return type.equals(claims.get("type", String.class));
  }

  public LocalDateTime refreshExpiresAt() {
    return LocalDateTime.now().plus(refreshExpiration);
  }
}
