package com.jjap.berries.concert.dto;

import com.jjap.berries.concert.domain.Concert;
import com.jjap.berries.concert.domain.ConcertStatus;
import java.time.LocalDateTime;

public record ConcertResponse(
    Long concertId,
    Long channelId,
    String title,
    String venue,
    LocalDateTime concertAt,
    LocalDateTime reservationStartAt,
    LocalDateTime reservationEndAt,
    ConcertStatus status) {
  public static ConcertResponse from(Concert concert) {
    return new ConcertResponse(
        concert.getId(),
        concert.getChannel().getId(),
        concert.getTitle(),
        concert.getVenue(),
        concert.getConcertAt(),
        concert.getReservationStartAt(),
        concert.getReservationEndAt(),
        concert.getStatus());
  }
}
