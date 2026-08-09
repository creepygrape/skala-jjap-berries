package com.jjap.berries.global.config;

import com.jjap.berries.global.security.JsonAccessDeniedHandler;
import com.jjap.berries.global.security.JsonAuthenticationEntryPoint;
import com.jjap.berries.global.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
  private final JwtAuthenticationFilter jwtFilter;
  private final JsonAuthenticationEntryPoint authenticationEntryPoint;
  private final JsonAccessDeniedHandler accessDeniedHandler;

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(c -> c.disable())
        .anonymous(a -> a.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .headers(h -> h.frameOptions(f -> f.sameOrigin()))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        "/api/auth/signup",
                        "/api/auth/login",
                        "/api/auth/refresh",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/h2-console/**",
                        "/actuator/health",
                        "/error")
                    .permitAll()
                    .requestMatchers("/api/analytics/**")
                    .hasRole("MANAGER")
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/channels/managed",
                        "/api/channels/*/members")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.GET, "/api/users/me")
                    .authenticated()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/users/*",
                        "/api/channels",
                        "/api/channels/*",
                        "/api/concerts/**",
                        "/api/seats/**",
                        "/api/products/**",
                        "/api/posts/**",
                        "/api/comments/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
