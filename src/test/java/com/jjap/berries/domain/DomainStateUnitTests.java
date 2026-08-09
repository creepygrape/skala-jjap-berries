package com.jjap.berries.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jjap.berries.channel.domain.Channel;
import com.jjap.berries.concert.domain.Concert;
import com.jjap.berries.concert.domain.Seat;
import com.jjap.berries.concert.domain.SeatStatus;
import com.jjap.berries.product.domain.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class DomainStateUnitTests {

  @Test
  void productStockCanBeDecreasedAndRestored() {
    Product product = new Product(artist(), "상품", "설명", BigDecimal.valueOf(10000), 3, null);

    product.decreaseStock(2);
    assertThat(product.getStock()).isEqualTo(1);

    product.restoreStock(2);
    assertThat(product.getStock()).isEqualTo(3);
  }

  @Test
  void productRejectsDecreaseBeyondStock() {
    Product product = new Product(artist(), "상품", "설명", BigDecimal.valueOf(10000), 1, null);

    assertThatThrownBy(() -> product.decreaseStock(2)).isInstanceOf(IllegalStateException.class);
    assertThat(product.getStock()).isEqualTo(1);
  }

  @Test
  void seatCanBeReservedAndMadeAvailableAgain() {
    LocalDateTime now = LocalDateTime.now();
    Concert concert =
        new Concert(artist(), "공연", "공연장", now.plusDays(2), now.minusDays(1), now.plusDays(1));
    Seat seat = new Seat(concert, "A", 1, "A-1", "VIP", BigDecimal.valueOf(100000));

    seat.reserve();
    assertThat(seat.getStatus()).isEqualTo(SeatStatus.RESERVED);

    assertThatThrownBy(seat::reserve).isInstanceOf(IllegalStateException.class);

    seat.makeAvailable();
    assertThat(seat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
  }

  private Channel artist() {
    return new Channel("DOMAIN UNIT " + System.nanoTime(), "test", null);
  }
}
