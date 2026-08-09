package com.jjap.berries.reservation.service;

import com.jjap.berries.concert.domain.Concert;
import com.jjap.berries.concert.domain.ConcertStatus;
import com.jjap.berries.concert.domain.Seat;
import com.jjap.berries.concert.domain.SeatStatus;
import com.jjap.berries.concert.repository.ConcertRepository;
import com.jjap.berries.concert.repository.SeatRepository;
import com.jjap.berries.global.exception.BusinessException;
import com.jjap.berries.global.exception.ErrorCode;
import com.jjap.berries.global.service.AccessService;
import com.jjap.berries.reservation.domain.Reservation;
import com.jjap.berries.reservation.domain.ReservationStatus;
import com.jjap.berries.reservation.dto.ReservationCreateRequest;
import com.jjap.berries.reservation.dto.ReservationResponse;
import com.jjap.berries.reservation.repository.ReservationRepository;
import com.jjap.berries.user.domain.UserRole;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {
  private final ReservationRepository reservations;
  private final ConcertRepository concerts;
  private final SeatRepository seats;
  private final AccessService access;

  @Transactional
  public ReservationResponse create(Long userId, Long concertId, ReservationCreateRequest request) {
    var user = access.user(userId);
    if (user.getRole() != UserRole.USER) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
    Concert concert = concert(concertId);
    LocalDateTime now = LocalDateTime.now();
    if (concert.getStatus() != ConcertStatus.ON_SALE
        || now.isAfter(concert.getReservationEndAt())) {
      throw new BusinessException(ErrorCode.RESERVATION_CLOSED);
    }
    if (now.isBefore(concert.getReservationStartAt())) {
      throw new BusinessException(ErrorCode.RESERVATION_NOT_OPEN);
    }
    Seat seat = lockedSeat(request.seatId());
    if (!seat.getConcert().getId().equals(concertId)) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }
    if (seat.getStatus() != SeatStatus.AVAILABLE) {
      throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE);
    }
    seat.reserve();
    return ReservationResponse.from(reservations.save(new Reservation(user, concert, seat)));
  }

  public List<ReservationResponse> list(Long userId) {
    access.user(userId);
    return reservations.findAllByUserIdOrderByReservedAtDesc(userId).stream()
        .map(ReservationResponse::from)
        .toList();
  }

  public ReservationResponse get(Long userId, Long id) {
    return ReservationResponse.from(owned(userId, id));
  }

  @Transactional
  public ReservationResponse cancel(Long userId, Long id) {
    Reservation reservation = owned(userId, id);
    if (reservation.getStatus() != ReservationStatus.RESERVED
        || !LocalDateTime.now().isBefore(reservation.getConcert().getConcertAt())) {
      throw new BusinessException(ErrorCode.RESERVATION_NOT_CANCELABLE);
    }
    reservation.cancel();
    reservation.getSeat().makeAvailable();
    return ReservationResponse.from(reservation);
  }

  private Reservation owned(Long userId, Long id) {
    Reservation reservation =
        reservations
            .findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    if (!reservation.getUser().getId().equals(userId)) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
    return reservation;
  }

  private Concert concert(Long id) {
    return concerts
        .findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.CONCERT_NOT_FOUND));
  }

  private Seat lockedSeat(Long id) {
    return seats
        .findByIdForUpdate(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
  }
}
