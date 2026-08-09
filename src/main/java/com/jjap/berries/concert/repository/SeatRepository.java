package com.jjap.berries.concert.repository;

import com.jjap.berries.concert.domain.Seat;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatRepository extends JpaRepository<Seat, Long> {
  boolean existsByConcertIdAndSectionAndSeatSequence(
      Long concertId, String section, Integer seatSequence);

  boolean existsByConcertIdAndSectionAndSeatSequenceIn(
      Long concertId, String section, Collection<Integer> seatSequences);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Seat s where s.id=:id")
  Optional<Seat> findByIdForUpdate(@Param("id") Long id);
}
