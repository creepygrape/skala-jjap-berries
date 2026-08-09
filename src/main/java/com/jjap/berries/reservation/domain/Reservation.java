package com.jjap.berries.reservation.domain;

import com.jjap.berries.concert.domain.Concert;
import com.jjap.berries.concert.domain.Seat;
import com.jjap.berries.global.common.domain.BaseEntity;
import com.jjap.berries.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(optional = false)
  @JoinColumn(name = "concert_id", nullable = false)
  private Concert concert;

  @ManyToOne(optional = false)
  @JoinColumn(name = "seat_id", nullable = false)
  private Seat seat;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private ReservationStatus status;

  @Column(nullable = false)
  private LocalDateTime reservedAt;

  private LocalDateTime cancelledAt;

  @Column(precision = 19, scale = 0)
  private BigDecimal reservedPrice;

  public Reservation(User user, Concert concert, Seat seat) {
    this.user = user;
    this.concert = concert;
    this.seat = seat;
    this.status = ReservationStatus.RESERVED;
    this.reservedAt = LocalDateTime.now();
    this.reservedPrice = seat.getPrice();
  }

  public void cancel() {
    this.status = ReservationStatus.CANCELLED;
    this.cancelledAt = LocalDateTime.now();
  }
}
