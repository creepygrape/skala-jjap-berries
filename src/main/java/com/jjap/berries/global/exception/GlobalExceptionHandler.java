package com.jjap.berries.global.exception;

import com.jjap.berries.global.common.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException exception) {
    ErrorCode errorCode = exception.getErrorCode();
    return ResponseEntity.status(errorCode.getStatus())
        .body(ApiErrorResponse.failure(errorCode.name(), errorCode.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidationException(
      MethodArgumentNotValidException exception) {
    String message =
        exception.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getDefaultMessage())
            .orElse(ErrorCode.INVALID_REQUEST.getMessage());

    return ResponseEntity.badRequest()
        .body(ApiErrorResponse.failure(ErrorCode.INVALID_REQUEST.name(), message));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
      ConstraintViolationException exception) {
    String message =
        exception.getConstraintViolations().stream()
            .findFirst()
            .map(violation -> violation.getMessage())
            .orElse(ErrorCode.INVALID_REQUEST.getMessage());
    return ResponseEntity.badRequest()
        .body(ApiErrorResponse.failure(ErrorCode.INVALID_REQUEST.name(), message));
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    ServletRequestBindingException.class
  })
  public ResponseEntity<ApiErrorResponse> handleMalformedRequest(Exception exception) {
    return ResponseEntity.badRequest()
        .body(
            ApiErrorResponse.failure(
                ErrorCode.INVALID_REQUEST.name(), ErrorCode.INVALID_REQUEST.getMessage()));
  }

  @ExceptionHandler(PropertyReferenceException.class)
  public ResponseEntity<ApiErrorResponse> handleInvalidSortProperty(
      PropertyReferenceException exception) {
    return ResponseEntity.badRequest()
        .body(
            ApiErrorResponse.failure(
                ErrorCode.INVALID_REQUEST.name(), exception.getMessage()));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
      DataIntegrityViolationException exception) {
    ErrorCode errorCode = ErrorCode.DATA_INTEGRITY_CONFLICT;
    return ResponseEntity.status(errorCode.getStatus())
        .body(ApiErrorResponse.failure(errorCode.name(), errorCode.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
      Exception exception, HttpServletRequest request) {
    log.error(
        "Unexpected error: method={}, uri={}",
        request.getMethod(),
        request.getRequestURI(),
        exception);

    ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(errorCode.getStatus())
        .body(ApiErrorResponse.failure(errorCode.name(), errorCode.getMessage()));
  }
}
