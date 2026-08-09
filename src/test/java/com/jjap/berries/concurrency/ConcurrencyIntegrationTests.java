package com.jjap.berries.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import com.jjap.berries.channel.domain.Channel;
import com.jjap.berries.channel.repository.ChannelRepository;
import com.jjap.berries.concert.domain.Concert;
import com.jjap.berries.concert.domain.Seat;
import com.jjap.berries.concert.domain.SeatStatus;
import com.jjap.berries.concert.repository.ConcertRepository;
import com.jjap.berries.concert.repository.SeatRepository;
import com.jjap.berries.global.exception.BusinessException;
import com.jjap.berries.order.dto.OrderCreateRequest;
import com.jjap.berries.order.dto.OrderItemRequest;
import com.jjap.berries.order.service.OrderService;
import com.jjap.berries.order.domain.Order;
import com.jjap.berries.order.domain.OrderItem;
import com.jjap.berries.order.repository.OrderRepository;
import com.jjap.berries.order.repository.OrderItemRepository;
import com.jjap.berries.product.domain.Product;
import com.jjap.berries.product.repository.ProductRepository;
import com.jjap.berries.reservation.dto.ReservationCreateRequest;
import com.jjap.berries.reservation.repository.ReservationRepository;
import com.jjap.berries.reservation.service.ReservationService;
import com.jjap.berries.user.domain.User;
import com.jjap.berries.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ConcurrencyIntegrationTests {
  private static final int CONCURRENT_REQUEST_COUNT = 100;

  @Autowired ReservationService reservationService;
  @Autowired OrderService orderService;
  @Autowired UserRepository users;
  @Autowired ChannelRepository channels;
  @Autowired ConcertRepository concerts;
  @Autowired SeatRepository seats;
  @Autowired ProductRepository products;
  @Autowired ReservationRepository reservations;
  @Autowired OrderRepository orders;
  @Autowired OrderItemRepository orderItems;

  @Test
  @DisplayName("100 concurrent reservations: 1 success / 99 rejected")
  void onlyOneReservationSucceedsForSameSeat() throws Exception {
    User first = users.save(new User("seat-first@berries.com", "encoded", "seat_first"));
    User second = users.save(new User("seat-second@berries.com", "encoded", "seat_second"));
    Channel artist = channels.save(new Channel("CONCURRENCY CHANNEL", "test", null));
    LocalDateTime now = LocalDateTime.now();
    Concert concert =
        concerts.save(
            new Concert(
                artist,
                "Concurrency Concert",
                "Test Hall",
                now.plusDays(2),
                now.minusDays(1),
                now.plusDays(1)));
    Seat seat =
        seats.save(new Seat(concert, "A", 1, "A-1", "VIP", BigDecimal.valueOf(100000)));
    long reservationCount = reservations.count();
    AtomicInteger success = new AtomicInteger();
    AtomicInteger rejected = new AtomicInteger();
    runConcurrently(
        CONCURRENT_REQUEST_COUNT,
        index -> {
          try {
            reservationService.create(
                index % 2 == 0 ? first.getId() : second.getId(),
                concert.getId(),
                new ReservationCreateRequest(seat.getId()));
            success.incrementAndGet();
          } catch (BusinessException ignored) {
            rejected.incrementAndGet();
          }
        });
    assertThat(success.get()).isEqualTo(1);
    assertThat(rejected.get()).isEqualTo(99);
    printResult("same-seat reservation", success, rejected);
    assertThat(seats.findById(seat.getId()).orElseThrow().getStatus())
        .isEqualTo(SeatStatus.RESERVED);
    assertThat(reservations.count()).isEqualTo(reservationCount + 1);
  }

  @Test
  @DisplayName("100 concurrent orders: 10 success / 90 rejected")
  void stockNeverDropsBelowZero() throws Exception {
    User buyer = users.save(new User("stock-buyer@berries.com", "encoded", "stock_buyer"));
    Channel artist = channels.save(new Channel("STOCK CHANNEL", "test", null));
    Product product =
        products.save(
            new Product(artist, "Limited Goods", "test", BigDecimal.valueOf(10000), 10, null));
    AtomicInteger success = new AtomicInteger();
    AtomicInteger rejected = new AtomicInteger();
    runConcurrently(
        CONCURRENT_REQUEST_COUNT,
        index -> {
          try {
            orderService.create(
                buyer.getId(),
                new OrderCreateRequest(List.of(new OrderItemRequest(product.getId(), 1))));
            success.incrementAndGet();
          } catch (BusinessException ignored) {
            rejected.incrementAndGet();
          }
        });
    assertThat(success.get()).isEqualTo(10);
    assertThat(rejected.get()).isEqualTo(90);
    printResult("limited-stock order", success, rejected);
    assertThat(products.findById(product.getId()).orElseThrow().getStock()).isZero();
  }

  @Test
  @DisplayName("100 concurrent cancellations: 1 success / 99 rejected")
  void concurrentOrderCancellationRestoresStockOnlyOnce() throws Exception {
    User buyer = users.save(new User("cancel-buyer@berries.com", "encoded", "cancel_buyer"));
    Channel channel = channels.save(new Channel("CANCEL CHANNEL", "test", null));
    Product product =
        products.save(
            new Product(channel, "Cancel Goods", "test", BigDecimal.TEN, 10, null));
    product.decreaseStock(2);
    products.save(product);
    Order order = orders.save(new Order(buyer, BigDecimal.valueOf(20)));
    orderItems.save(new OrderItem(order, product, BigDecimal.TEN, 2));
    AtomicInteger success = new AtomicInteger();
    AtomicInteger rejected = new AtomicInteger();

    runConcurrently(
        CONCURRENT_REQUEST_COUNT,
        index -> {
          try {
            orderService.cancel(buyer.getId(), order.getId());
            success.incrementAndGet();
          } catch (BusinessException ignored) {
            rejected.incrementAndGet();
          }
        });

    assertThat(success.get()).isEqualTo(1);
    assertThat(rejected.get()).isEqualTo(99);
    printResult("same-order cancellation", success, rejected);
    assertThat(products.findById(product.getId()).orElseThrow().getStock()).isEqualTo(10);
  }

  private void runConcurrently(int count, ThrowingConsumer task) throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(count);
    CountDownLatch ready = new CountDownLatch(count);
    CountDownLatch start = new CountDownLatch(1);
    try {
      List<Future<Object>> futures =
          java.util.stream.IntStream.range(0, count)
              .mapToObj(
                  i ->
                      pool.submit(
                          () -> {
                            ready.countDown();
                            start.await();
                            task.accept(i);
                            return null;
                          }))
              .toList();
      assertThat(ready.await(15, TimeUnit.SECONDS)).isTrue();
      start.countDown();
      for (Future<?> future : futures) future.get(30, TimeUnit.SECONDS);
    } finally {
      pool.shutdownNow();
    }
  }

  private void printResult(
      String scenario, AtomicInteger success, AtomicInteger rejected) {
    System.out.printf(
        "[CONCURRENCY RESULT] %s: total=%d, success=%d, rejected=%d%n",
        scenario, CONCURRENT_REQUEST_COUNT, success.get(), rejected.get());
  }

  @FunctionalInterface
  interface ThrowingConsumer {
    void accept(int index) throws Exception;
  }
}
