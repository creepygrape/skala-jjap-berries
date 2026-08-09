package com.jjap.berries.concert.repository;

import com.jjap.berries.concert.domain.Concert;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertRepository extends JpaRepository<Concert, Long> {
  List<Concert> findAllByChannelIdOrderByConcertAtAsc(Long channelId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from Concert c where c.id = :id")
  Optional<Concert> findByIdForUpdate(@Param("id") Long id);
}
