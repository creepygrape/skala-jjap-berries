package com.jjap.berries.global;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jjap.berries.global.aop.ApiLoggingAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;

@ExtendWith(OutputCaptureExtension.class)
class ApiLoggingAspectTests {

  @Test
  void logsMethodUriAndElapsedTimeOnSuccess(CapturedOutput output) throws Throwable {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products/1");
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    when(joinPoint.proceed()).thenReturn("response");

    Object result = new ApiLoggingAspect(request).logApi(joinPoint);

    assertThat(result).isEqualTo("response");
    assertThat(output)
        .contains("API success")
        .contains("method=GET")
        .contains("uri=/api/products/1")
        .contains("elapsedMs=");
  }

  @Test
  void logsMethodUriAndExceptionOnFailure(CapturedOutput output) throws Throwable {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/orders");
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    when(joinPoint.proceed()).thenThrow(new IllegalStateException("test failure"));
    ApiLoggingAspect aspect = new ApiLoggingAspect(request);

    assertThatThrownBy(() -> aspect.logApi(joinPoint))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("test failure");
    assertThat(output)
        .contains("API failure")
        .contains("method=POST")
        .contains("uri=/api/orders")
        .contains("exception=IllegalStateException")
        .contains("message=test failure");
  }
}
