package com.jjap.berries.global.security;

import com.jjap.berries.auth.service.RevokedAccessTokenService;
import com.jjap.berries.global.exception.ErrorCode;
import com.jjap.berries.user.domain.UserStatus;
import com.jjap.berries.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  public static final String ACCESS_TOKEN_ATTRIBUTE =
      JwtAuthenticationFilter.class.getName() + ".accessToken";

  private final JwtTokenProvider tokens;
  private final UserRepository users;
  private final SecurityErrorResponder errors;
  private final RevokedAccessTokenService revokedAccessTokens;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header == null) {
      chain.doFilter(request, response);
      return;
    }
    if (!header.startsWith("Bearer ") || header.substring(7).isBlank()) {
      errors.write(response, ErrorCode.INVALID_TOKEN);
      return;
    }
    try {
      String token = header.substring(7);
      if (revokedAccessTokens.isRevoked(token)) {
        errors.write(response, ErrorCode.INVALID_TOKEN);
        return;
      }
      Claims claims = tokens.parse(token);
      if (!tokens.isType(claims, "access")) {
        errors.write(response, ErrorCode.INVALID_TOKEN);
        return;
      }
      var user =
          users
              .findById(tokens.userId(claims))
              .filter(u -> u.getStatus() == UserStatus.ACTIVE)
              .orElse(null);
      if (user == null) {
        errors.write(response, ErrorCode.INVALID_TOKEN);
        return;
      }
      var principal = new AuthenticatedUser(user.getId(), user.getRole());
      var authentication =
          new UsernamePasswordAuthenticationToken(
              principal,
              null,
              List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
      SecurityContextHolder.getContext().setAuthentication(authentication);
      request.setAttribute(ACCESS_TOKEN_ATTRIBUTE, token);
      chain.doFilter(request, response);
    } catch (ExpiredJwtException exception) {
      SecurityContextHolder.clearContext();
      errors.write(response, ErrorCode.EXPIRED_TOKEN);
    } catch (JwtException | IllegalArgumentException exception) {
      SecurityContextHolder.clearContext();
      errors.write(response, ErrorCode.INVALID_TOKEN);
    }
  }
}
