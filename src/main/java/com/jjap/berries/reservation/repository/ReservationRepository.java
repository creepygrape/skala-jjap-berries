package com.jjap.berries.reservation.repository;

import com.jjap.berries.reservation.domain.Reservation;
import java.util.List;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
  List<Reservation> findAllByUserIdOrderByReservedAtDesc(Long userId);

  boolean existsBySeatId(Long seatId);

  boolean existsBySeatIdIn(Collection<Long> seatIds);
}
