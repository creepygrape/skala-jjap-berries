package com.jjap.berries.analytics.service;

import com.jjap.berries.analytics.dto.ChannelSalesReportResponse;
import com.jjap.berries.analytics.dto.DailyChannelSalesResponse;
import com.jjap.berries.analytics.dto.ProductSalesResponse;
import com.jjap.berries.analytics.dto.ConcertSalesResponse;
import com.jjap.berries.analytics.dto.SeatGradeSalesResponse;
import com.jjap.berries.analytics.mapper.ChannelSalesMapper;
import com.jjap.berries.global.exception.BusinessException;
import com.jjap.berries.global.exception.ErrorCode;
import com.jjap.berries.global.service.AccessService;
import com.jjap.berries.user.domain.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChannelSalesService {

  private static final long MAX_RANGE_DAYS = 366;

  private final ChannelSalesMapper salesMapper;
  private final AccessService access;

  public ChannelSalesReportResponse get(Long userId, Long channelId, LocalDate from, LocalDate to) {
    DateRange range = range(from, to);
    from = range.from();
    to = range.to();

    User user = access.user(userId);
    access.manager(user, channelId);
    String channelName =
        salesMapper
            .findChannelName(channelId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

    List<DailyChannelSalesResponse> dailySales =
        salesMapper.findDailySales(channelId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());

    long totalOrderCount =
        dailySales.stream().mapToLong(DailyChannelSalesResponse::orderCount).sum();
    long totalQuantitySold =
        dailySales.stream().mapToLong(DailyChannelSalesResponse::quantitySold).sum();
    BigDecimal totalSalesAmount =
        dailySales.stream()
            .map(DailyChannelSalesResponse::totalSalesAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    long totalReservationCount =
        dailySales.stream().mapToLong(DailyChannelSalesResponse::reservationCount).sum();
    BigDecimal totalProductSalesAmount =
        dailySales.stream()
            .map(DailyChannelSalesResponse::productSalesAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal totalConcertSalesAmount =
        dailySales.stream()
            .map(DailyChannelSalesResponse::concertSalesAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return new ChannelSalesReportResponse(
        channelId,
        channelName,
        from,
        to,
        totalOrderCount,
        totalQuantitySold,
        totalProductSalesAmount,
        totalReservationCount,
        totalConcertSalesAmount,
        totalSalesAmount,
        dailySales);
  }

  public List<ProductSalesResponse> getProductSales(
      Long userId, Long channelId, LocalDate from, LocalDate to) {
    DateRange range = range(from, to);
    validateManager(userId, channelId);
    return salesMapper.findProductSales(
        channelId, range.from().atStartOfDay(), range.to().plusDays(1).atStartOfDay());
  }

  public List<ConcertSalesResponse> getConcertSales(
      Long userId, Long channelId, LocalDate from, LocalDate to) {
    DateRange range = range(from, to);
    validateManager(userId, channelId);
    return salesMapper.findConcertSales(
        channelId, range.from().atStartOfDay(), range.to().plusDays(1).atStartOfDay());
  }

  public List<SeatGradeSalesResponse> getSeatGradeSales(
      Long userId, Long channelId, Long concertId) {
    validateManager(userId, channelId);
    Long concertChannelId =
        salesMapper
            .findConcertChannelId(concertId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CONCERT_NOT_FOUND));
    if (!concertChannelId.equals(channelId)) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }
    return salesMapper.findSeatGradeSales(concertId);
  }

  private void validateManager(Long userId, Long channelId) {
    access.manager(access.user(userId), channelId);
  }

  private DateRange range(LocalDate from, LocalDate to) {
    LocalDate today = LocalDate.now();
    LocalDate resolvedFrom = from == null ? today : from;
    LocalDate resolvedTo = to == null ? today : to;
    validateRange(resolvedFrom, resolvedTo);
    return new DateRange(resolvedFrom, resolvedTo);
  }

  private void validateRange(LocalDate from, LocalDate to) {
    if (from == null
        || to == null
        || from.isAfter(to)
        || ChronoUnit.DAYS.between(from, to) >= MAX_RANGE_DAYS) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }
  }

  private record DateRange(LocalDate from, LocalDate to) {}
}
