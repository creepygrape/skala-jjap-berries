package com.jjap.berries.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jjap.berries.global.common.response.ApiErrorResponse;
import com.jjap.berries.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityErrorResponder {
  private final ObjectMapper objectMapper;

  public void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
    response.setStatus(errorCode.getStatus().value());
    response.setCharacterEncoding("UTF-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(
        response.getWriter(), ApiErrorResponse.failure(errorCode.name(), errorCode.getMessage()));
  }
}
