package com.jjap.berries.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
  EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
  NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
  INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
  EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "채널을 찾을 수 없습니다."),
  CHANNEL_USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 다른 채널에 소속된 사용자입니다."),
  CANNOT_REMOVE_SELF(HttpStatus.CONFLICT, "자기 자신의 매니저 권한은 제거할 수 없습니다."),
  SAME_CHANNEL_USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 채널에 소속된 사용자입니다."),
  NOT_ARTIST_MEMBER(HttpStatus.FORBIDDEN, "해당 채널의 소속된 계정이 아닙니다."),
  NOT_MANAGER(HttpStatus.FORBIDDEN, "해당 채널의 매니저가 아닙니다."),
  MEMBERSHIP_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 가입한 커뮤니티입니다."),
  NOT_COMMUNITY_MEMBER(HttpStatus.FORBIDDEN, "커뮤니티 가입이 필요합니다."),
  POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
  COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
  LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 좋아요를 등록했습니다."),
  LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "좋아요를 찾을 수 없습니다."),
  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
  PRODUCT_NOT_ON_SALE(HttpStatus.CONFLICT, "판매 중인 상품이 아닙니다."),
  OUT_OF_STOCK(HttpStatus.CONFLICT, "상품 재고가 부족합니다."),
  ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
  ORDER_NOT_CANCELABLE(HttpStatus.CONFLICT, "주문을 취소할 수 없습니다."),
  CONCERT_NOT_FOUND(HttpStatus.NOT_FOUND, "공연을 찾을 수 없습니다."),
  CONCERT_SCHEDULE_UPDATE_CLOSED(HttpStatus.CONFLICT, "예매 시작 후에는 공연 및 예매 일시를 수정할 수 없습니다."),
  RESERVATION_NOT_OPEN(HttpStatus.CONFLICT, "예매 기간이 시작되지 않았습니다."),
  RESERVATION_CLOSED(HttpStatus.CONFLICT, "예매가 종료되었습니다."),
  SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "좌석을 찾을 수 없습니다."),
  SEAT_NOT_AVAILABLE(HttpStatus.CONFLICT, "예매 가능한 좌석이 아닙니다."),
  SEAT_HAS_RESERVATION_HISTORY(HttpStatus.CONFLICT, "예매 이력이 있는 좌석은 삭제할 수 없습니다."),
  SEAT_DELETION_CLOSED(HttpStatus.CONFLICT, "예매 시작 후에는 좌석을 삭제할 수 없습니다."),
  SEAT_UPDATE_CLOSED(HttpStatus.CONFLICT, "예매 시작 후에는 좌석을 수정할 수 없습니다."),
  SEAT_CREATION_CLOSED(HttpStatus.CONFLICT, "예매 시작 후에는 좌석을 등록할 수 없습니다."),
  SEAT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 좌석입니다."),
  RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예매 내역을 찾을 수 없습니다."),
  RESERVATION_NOT_CANCELABLE(HttpStatus.CONFLICT, "취소할 수 없는 예매입니다."),
  DATA_INTEGRITY_CONFLICT(HttpStatus.CONFLICT, "이미 존재하거나 다른 데이터에서 사용 중인 값입니다.");

  private final HttpStatus status;
  private final String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }
}
