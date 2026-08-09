package com.jjap.berries.analytics.mapper;

import com.jjap.berries.analytics.dto.DailyChannelSalesResponse;
import com.jjap.berries.analytics.dto.ProductSalesResponse;
import com.jjap.berries.analytics.dto.ConcertSalesResponse;
import com.jjap.berries.analytics.dto.SeatGradeSalesResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChannelSalesMapper {

  Optional<String> findChannelName(@Param("channelId") Long channelId);

  List<DailyChannelSalesResponse> findDailySales(
      @Param("channelId") Long channelId,
      @Param("from") LocalDateTime from,
      @Param("toExclusive") LocalDateTime toExclusive);

  List<ProductSalesResponse> findProductSales(
      @Param("channelId") Long channelId,
      @Param("from") LocalDateTime from,
      @Param("toExclusive") LocalDateTime toExclusive);

  List<ConcertSalesResponse> findConcertSales(
      @Param("channelId") Long channelId,
      @Param("from") LocalDateTime from,
      @Param("toExclusive") LocalDateTime toExclusive);

  Optional<Long> findConcertChannelId(@Param("concertId") Long concertId);

  List<SeatGradeSalesResponse> findSeatGradeSales(@Param("concertId") Long concertId);
}
