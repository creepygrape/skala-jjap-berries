package com.jjap.berries.global;

import static org.assertj.core.api.Assertions.assertThat;

import com.jjap.berries.global.common.response.ApiErrorResponse;
import com.jjap.berries.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTests {

  @Test
  void unexpectedExceptionReturnsGenericResponseWithoutInternalDetails() {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");

    ResponseEntity<ApiErrorResponse> response =
        handler.handleUnexpectedException(
            new IllegalStateException("database password must not be exposed"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody())
        .isEqualTo(
            ApiErrorResponse.failure(
                "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
    assertThat(response.getBody().message()).doesNotContain("database password");
  }
}
