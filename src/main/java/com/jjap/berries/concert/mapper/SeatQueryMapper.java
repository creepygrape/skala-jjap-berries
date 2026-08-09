package com.jjap.berries.concert.mapper;

import com.jjap.berries.concert.domain.SeatStatus;
import com.jjap.berries.concert.dto.SeatResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SeatQueryMapper {
  List<SeatResponse> findSeats(
      @Param("concertId") Long concertId,
      @Param("section") String section,
      @Param("grade") String grade,
      @Param("status") SeatStatus status,
      @Param("limit") int limit,
      @Param("offset") long offset);

  Integer findMaxSeatSequence(
      @Param("concertId") Long concertId, @Param("section") String section);

  long countSeats(
      @Param("concertId") Long concertId,
      @Param("section") String section,
      @Param("grade") String grade,
      @Param("status") SeatStatus status);
}
