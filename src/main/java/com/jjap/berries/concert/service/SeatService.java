package com.jjap.berries.concert.service;

import com.jjap.berries.concert.domain.Concert;
import com.jjap.berries.concert.domain.Seat;
import com.jjap.berries.concert.domain.SeatStatus;
import com.jjap.berries.concert.dto.SeatBulkRequest;
import com.jjap.berries.concert.dto.SeatBulkCreateResponse;
import com.jjap.berries.concert.dto.SeatCreateRequest;
import com.jjap.berries.concert.dto.SeatResponse;
import com.jjap.berries.concert.dto.SeatPageResponse;
import com.jjap.berries.concert.dto.SeatUpdateRequest;
import com.jjap.berries.concert.repository.ConcertRepository;
import com.jjap.berries.concert.repository.SeatRepository;
import com.jjap.berries.concert.mapper.SeatQueryMapper;
import com.jjap.berries.global.exception.BusinessException;
import com.jjap.berries.global.exception.ErrorCode;
import com.jjap.berries.global.service.AccessService;
import com.jjap.berries.reservation.repository.ReservationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {
  private final SeatRepository seats;
  private final ConcertRepository concerts;
  private final AccessService access;
  private final ReservationRepository reservations;
  private final SeatQueryMapper seatQueries;

  public SeatPageResponse list(
      Long concertId, String section, String grade, SeatStatus status, int page, int size) {
    concert(concertId);
    List<SeatResponse> content =
        seatQueries.findSeats(concertId, section, grade, status, size, (long) page * size);
    long total = seatQueries.countSeats(concertId, section, grade, status);
    return SeatPageResponse.of(content, page, size, total);
  }

  @Transactional
  public SeatResponse create(Long userId, Long concertId, SeatCreateRequest request) {
    Concert concert = lockedConcert(concertId);
    access.manager(access.user(userId), concert.getChannel().getId());
    validateCreationOpen(concert);
    validateDuplicate(concertId, request);
    return SeatResponse.from(
        seats.save(
            new Seat(
                concert,
                request.section(),
                request.seatSequence(),
                request.seatLabel(),
                request.grade(),
                request.price())));
  }

  @Transactional
  public SeatBulkCreateResponse bulkCreate(
      Long userId, Long concertId, SeatBulkRequest request) {
    Concert concert = lockedConcert(concertId);
    access.manager(access.user(userId), concert.getChannel().getId());
    validateCreationOpen(concert);
    int startNumber =
        request.startNumber() == null
            ? nextSeatNumber(concertId, request.section())
            : request.startNumber();
    if ((long) startNumber + request.count() - 1 > 1_000_000) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }
    List<Integer> seatSequences =
        IntStream.range(startNumber, startNumber + request.count())
            .boxed()
            .toList();
    if (seats.existsByConcertIdAndSectionAndSeatSequenceIn(
        concertId, request.section(), seatSequences)) {
      throw new BusinessException(ErrorCode.SEAT_ALREADY_EXISTS);
    }
    List<Seat> newSeats =
        seatSequences.stream()
            .map(
                seatSequence ->
                    new Seat(
                        concert,
                        request.section(),
                        seatSequence,
                        request.section() + "-" + seatSequence,
                        request.grade(),
                        request.price()))
            .toList();
    List<SeatResponse> created =
        seats.saveAll(newSeats).stream().map(SeatResponse::from).toList();
    return new SeatBulkCreateResponse(
        request.section(),
        startNumber,
        startNumber + request.count() - 1,
        created.size(),
        created);
  }

  @Transactional
  public SeatResponse update(Long userId, Long id, SeatUpdateRequest request) {
    Seat seat = lockedSeat(id);
    access.manager(access.user(userId), seat.getConcert().getChannel().getId());
    validateUpdateOpen(seat.getConcert());
    if (seat.getStatus() != SeatStatus.AVAILABLE
        && (request.section() != null
            || request.seatSequence() != null
            || request.seatLabel() != null
            || request.grade() != null
            || request.price() != null
            || request.status() == SeatStatus.AVAILABLE)) {
      throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE);
    }
    String section = request.section() == null ? seat.getSection() : request.section();
    Integer sequence =
        request.seatSequence() == null ? seat.getSeatSequence() : request.seatSequence();
    if ((!section.equals(seat.getSection()) || !sequence.equals(seat.getSeatSequence()))
        && seats.existsByConcertIdAndSectionAndSeatSequence(
            seat.getConcert().getId(), section, sequence)) {
      throw new BusinessException(ErrorCode.SEAT_ALREADY_EXISTS);
    }
    seat.update(
        request.section(),
        request.seatSequence(),
        request.seatLabel(),
        request.grade(),
        request.price());
    if (request.status() == SeatStatus.RESERVED && seat.getStatus() == SeatStatus.AVAILABLE) {
      seat.reserve();
    }
    return SeatResponse.from(seat);
  }

  @Transactional
  public void delete(Long userId, Long id) {
    Seat seat = lockedSeat(id);
    access.manager(access.user(userId), seat.getConcert().getChannel().getId());
    validateDeletionOpen(seat.getConcert());
    validateDeletable(seat);
    seats.delete(seat);
  }

  private void validateDeletable(Seat seat) {
    if (seat.getStatus() != SeatStatus.AVAILABLE) {
      throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE);
    }
    if (reservations.existsBySeatId(seat.getId())) {
      throw new BusinessException(ErrorCode.SEAT_HAS_RESERVATION_HISTORY);
    }
  }

  private void validateDeletionOpen(Concert concert) {
    if (!LocalDateTime.now().isBefore(concert.getReservationStartAt())) {
      throw new BusinessException(ErrorCode.SEAT_DELETION_CLOSED);
    }
  }

  private void validateUpdateOpen(Concert concert) {
    if (!LocalDateTime.now().isBefore(concert.getReservationStartAt())) {
      throw new BusinessException(ErrorCode.SEAT_UPDATE_CLOSED);
    }
  }

  private void validateCreationOpen(Concert concert) {
    if (!LocalDateTime.now().isBefore(concert.getReservationStartAt())) {
      throw new BusinessException(ErrorCode.SEAT_CREATION_CLOSED);
    }
  }

  private Concert concert(Long id) {
    return concerts
        .findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.CONCERT_NOT_FOUND));
  }

  private Concert lockedConcert(Long id) {
    return concerts
        .findByIdForUpdate(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.CONCERT_NOT_FOUND));
  }

  private int nextSeatNumber(Long concertId, String section) {
    Integer maxSequence = seatQueries.findMaxSeatSequence(concertId, section);
    return maxSequence == null ? 1 : maxSequence + 1;
  }

  private Seat lockedSeat(Long id) {
    return seats
        .findByIdForUpdate(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
  }

  private void validateDuplicate(Long concertId, SeatCreateRequest request) {
    if (seats.existsByConcertIdAndSectionAndSeatSequence(
        concertId, request.section(), request.seatSequence())) {
      throw new BusinessException(ErrorCode.SEAT_ALREADY_EXISTS);
    }
  }
}
