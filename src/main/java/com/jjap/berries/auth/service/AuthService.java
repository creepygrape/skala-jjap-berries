package com.jjap.berries.auth.service;

import com.jjap.berries.auth.domain.RefreshToken;
import com.jjap.berries.auth.dto.LoginRequest;
import com.jjap.berries.auth.dto.SignupRequest;
import com.jjap.berries.auth.dto.SignupResponse;
import com.jjap.berries.auth.dto.TokenRequest;
import com.jjap.berries.auth.dto.TokenResponse;
import com.jjap.berries.auth.repository.RefreshTokenRepository;
import com.jjap.berries.global.exception.BusinessException;
import com.jjap.berries.global.exception.ErrorCode;
import com.jjap.berries.global.security.JwtTokenProvider;
import com.jjap.berries.user.domain.User;
import com.jjap.berries.user.domain.UserStatus;
import com.jjap.berries.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenRepository refreshTokens;
  private final JwtTokenProvider tokens;
  private final RevokedAccessTokenService revokedAccessTokens;

  @Transactional
  public SignupResponse signup(SignupRequest request) {
    validateDuplicate(request);

    User user =
        new User(
            request.email(),
            passwordEncoder.encode(request.password()),
            request.nickname(),
            request.role());

    return SignupResponse.from(userRepository.save(user));
  }

  @Transactional
  public TokenResponse login(LoginRequest request) {
    User user =
        userRepository
            .findByEmail(request.email())
            .filter(u -> u.getStatus() == UserStatus.ACTIVE)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
    if (!passwordEncoder.matches(request.password(), user.getPassword()))
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    return issue(user);
  }

  @Transactional
  public TokenResponse refresh(TokenRequest request) {
    try {
      Claims claims = tokens.parse(request.refreshToken());
      if (!tokens.isType(claims, "refresh")) {
        throw new BusinessException(ErrorCode.INVALID_TOKEN);
      }
      RefreshToken saved =
          refreshTokens
              .findByToken(request.refreshToken())
              .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
      if (saved.getExpiresAt().isBefore(LocalDateTime.now())
          || !saved.getUser().getId().equals(tokens.userId(claims))
          || saved.getUser().getStatus() != UserStatus.ACTIVE)
        throw new BusinessException(ErrorCode.INVALID_TOKEN);
      return issue(saved.getUser());
    } catch (ExpiredJwtException e) {
      throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
    } catch (JwtException | IllegalArgumentException e) {
      throw new BusinessException(ErrorCode.INVALID_TOKEN);
    }
  }

  @Transactional
  public void logout(Long userId, String accessToken) {
    Claims claims = tokens.parse(accessToken);
    if (!tokens.isType(claims, "access") || !userId.equals(tokens.userId(claims))) {
      throw new BusinessException(ErrorCode.INVALID_TOKEN);
    }
    revokedAccessTokens.revoke(accessToken, claims);
    refreshTokens.deleteByUserId(userId);
  }

  private TokenResponse issue(User user) {
    String access = tokens.createAccessToken(user);
    String refresh = tokens.createRefreshToken(user);
    refreshTokens
        .findByUserId(user.getId())
        .ifPresentOrElse(
            t -> t.rotate(refresh, tokens.refreshExpiresAt()),
            () -> refreshTokens.save(new RefreshToken(user, refresh, tokens.refreshExpiresAt())));
    return new TokenResponse(access, refresh);
  }

  private void validateDuplicate(SignupRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
    if (userRepository.existsByNickname(request.nickname())) {
      throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
    }
  }
}
