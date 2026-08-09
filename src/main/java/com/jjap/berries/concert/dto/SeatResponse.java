package com.jjap.berries.concert.dto;

import com.jjap.berries.concert.domain.Seat;
import com.jjap.berries.concert.domain.SeatStatus;
import java.math.BigDecimal;

public record SeatResponse(
    Long seatId,
    Long concertId,
    String section,
    Integer seatSequence,
    String seatLabel,
    String grade,
    BigDecimal price,
    SeatStatus status) {
  public static SeatResponse from(Seat seat) {
    return new SeatResponse(
        seat.getId(),
        seat.getConcert().getId(),
        seat.getSection(),
        seat.getSeatSequence(),
        seat.getSeatLabel(),
        seat.getGrade(),
        seat.getPrice(),
        seat.getStatus());
  }
}
