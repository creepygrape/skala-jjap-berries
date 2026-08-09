package com.jjap.berries.global.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ApiLoggingAspect {

  private final HttpServletRequest request;

  @Around("within(@org.springframework.web.bind.annotation.RestController *)")
  public Object logApi(ProceedingJoinPoint joinPoint) throws Throwable {
    String method = request.getMethod();
    String uri = request.getRequestURI();
    long startedAt = System.nanoTime();

    try {
      Object result = joinPoint.proceed();
      long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
      log.info("API success method={} uri={} elapsedMs={}", method, uri, elapsedMillis);
      return result;
    } catch (Throwable exception) {
      log.warn(
          "API failure method={} uri={} exception={} message={}",
          method,
          uri,
          exception.getClass().getSimpleName(),
          exception.getMessage());
      throw exception;
    }
  }
}
