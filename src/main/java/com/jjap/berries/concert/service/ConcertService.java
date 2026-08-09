package com.jjap.berries.concert.service;

import com.jjap.berries.channel.domain.Channel;
import com.jjap.berries.channel.repository.ChannelRepository;
import com.jjap.berries.concert.domain.Concert;
import com.jjap.berries.concert.domain.ConcertStatus;
import com.jjap.berries.concert.dto.ConcertCreateRequest;
import com.jjap.berries.concert.dto.ConcertResponse;
import com.jjap.berries.concert.dto.ConcertUpdateRequest;
import com.jjap.berries.concert.repository.ConcertRepository;
import com.jjap.berries.global.exception.BusinessException;
import com.jjap.berries.global.exception.ErrorCode;
import com.jjap.berries.global.service.AccessService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcertService {
  private final ConcertRepository concerts;
  private final ChannelRepository channels;
  private final AccessService access;

  public List<ConcertResponse> list(Long channelId) {
    channel(channelId);
    return concerts.findAllByChannelIdOrderByConcertAtAsc(channelId).stream()
        .map(ConcertResponse::from)
        .toList();
  }

  public ConcertResponse get(Long id) {
    return ConcertResponse.from(concert(id));
  }

  @Transactional
  public ConcertResponse create(Long userId, Long channelId, ConcertCreateRequest request) {
    access.manager(access.user(userId), channelId);
    validateNewSchedule(
        request.reservationStartAt(), request.reservationEndAt(), request.concertAt());
    return ConcertResponse.from(
        concerts.save(
            new Concert(
                channel(channelId),
                request.title(),
                request.venue(),
                request.concertAt(),
                request.reservationStartAt(),
                request.reservationEndAt())));
  }

  @Transactional
  public ConcertResponse update(Long userId, Long id, ConcertUpdateRequest request) {
    Concert concert = lockedConcert(id);
    access.manager(access.user(userId), concert.getChannel().getId());
    if (hasScheduleChange(request)) {
      if (!LocalDateTime.now().isBefore(concert.getReservationStartAt())) {
        throw new BusinessException(ErrorCode.CONCERT_SCHEDULE_UPDATE_CLOSED);
      }
      validateMerged(concert, request);
    }
    concert.update(
        request.title(),
        request.venue(),
        request.concertAt(),
        request.reservationStartAt(),
        request.reservationEndAt());
    return ConcertResponse.from(concert);
  }

  @Transactional
  public ConcertResponse status(Long userId, Long id, ConcertStatus status) {
    Concert concert = concert(id);
    access.manager(access.user(userId), concert.getChannel().getId());
    concert.changeStatus(status);
    return ConcertResponse.from(concert);
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

  private Channel channel(Long id) {
    return channels
        .findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));
  }

  private void validateMerged(Concert concert, ConcertUpdateRequest request) {
    LocalDateTime start =
        request.reservationStartAt() == null
            ? concert.getReservationStartAt()
            : request.reservationStartAt();
    LocalDateTime end =
        request.reservationEndAt() == null
            ? concert.getReservationEndAt()
            : request.reservationEndAt();
    LocalDateTime at = request.concertAt() == null ? concert.getConcertAt() : request.concertAt();
    validateNewSchedule(start, end, at);
  }

  private boolean hasScheduleChange(ConcertUpdateRequest request) {
    return request.concertAt() != null
        || request.reservationStartAt() != null
        || request.reservationEndAt() != null;
  }

  private void validateNewSchedule(LocalDateTime start, LocalDateTime end, LocalDateTime at) {
    if (!start.isAfter(LocalDateTime.now()) || start.isAfter(end) || end.isAfter(at)) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }
  }
}
