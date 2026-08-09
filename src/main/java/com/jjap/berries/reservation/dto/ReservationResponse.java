package com.jjap.berries.reservation.dto;

import com.jjap.berries.concert.domain.Seat;
import com.jjap.berries.reservation.domain.Reservation;
import com.jjap.berries.reservation.domain.ReservationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponse(
    Long reservationId,
    Long concertId,
    String concertTitle,
    Long seatId,
    String section,
    Integer seatSequence,
    String seatLabel,
    String grade,
    BigDecimal price,
    ReservationStatus status,
    LocalDateTime reservedAt,
    LocalDateTime cancelledAt) {
  public static ReservationResponse from(Reservation reservation) {
    Seat seat = reservation.getSeat();
    return new ReservationResponse(
        reservation.getId(),
        reservation.getConcert().getId(),
        reservation.getConcert().getTitle(),
        seat.getId(),
        seat.getSection(),
        seat.getSeatSequence(),
        seat.getSeatLabel(),
        seat.getGrade(),
        reservation.getReservedPrice() == null ? seat.getPrice() : reservation.getReservedPrice(),
        reservation.getStatus(),
        reservation.getReservedAt(),
        reservation.getCancelledAt());
  }
}
