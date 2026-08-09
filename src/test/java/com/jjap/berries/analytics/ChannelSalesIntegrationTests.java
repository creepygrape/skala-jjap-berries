package com.jjap.berries.analytics;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jjap.berries.channel.domain.Channel;
import com.jjap.berries.channel.domain.ChannelUser;
import com.jjap.berries.channel.repository.ChannelRepository;
import com.jjap.berries.channel.repository.ChannelUserRepository;
import com.jjap.berries.global.security.JwtTokenProvider;
import com.jjap.berries.concert.domain.Concert;
import com.jjap.berries.concert.domain.Seat;
import com.jjap.berries.concert.repository.ConcertRepository;
import com.jjap.berries.concert.repository.SeatRepository;
import com.jjap.berries.order.domain.Order;
import com.jjap.berries.order.domain.OrderItem;
import com.jjap.berries.order.repository.OrderItemRepository;
import com.jjap.berries.order.repository.OrderRepository;
import com.jjap.berries.product.domain.Product;
import com.jjap.berries.product.repository.ProductRepository;
import com.jjap.berries.reservation.domain.Reservation;
import com.jjap.berries.reservation.repository.ReservationRepository;
import com.jjap.berries.user.domain.User;
import com.jjap.berries.user.domain.UserRole;
import com.jjap.berries.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ChannelSalesIntegrationTests {

  @Autowired MockMvc mvc;
  @Autowired JwtTokenProvider tokens;
  @Autowired UserRepository users;
  @Autowired ChannelRepository channels;
  @Autowired ChannelUserRepository channelUsers;
  @Autowired ProductRepository products;
  @Autowired OrderRepository orders;
  @Autowired OrderItemRepository orderItems;
  @Autowired ConcertRepository concerts;
  @Autowired SeatRepository seats;
  @Autowired ReservationRepository reservations;

  @Test
  void managerCanViewAssignedChannelSalesAndCancelledOrdersAreExcluded() throws Exception {
    String suffix = Long.toString(System.nanoTime());
    User manager = user("sales_manager_" + suffix, UserRole.MANAGER);
    User buyer = user("sales_buyer_" + suffix, UserRole.USER);
    Channel channel = channels.save(new Channel("SALES CHANNEL " + suffix, "sales test", null));
    channelUsers.save(new ChannelUser(channel, manager));
    Product product =
        products.save(
            new Product(
                channel,
                "Sales Product",
                "sales test product",
                BigDecimal.valueOf(10_000),
                100,
                null));

    order(buyer, product, 2, false);
    order(buyer, product, 1, false);
    order(buyer, product, 5, true);
    LocalDateTime now = LocalDateTime.now();
    Concert concert =
        concerts.save(
            new Concert(
                channel,
                "Sales Concert",
                "Sales Hall",
                now.plusDays(2),
                now.minusDays(1),
                now.plusDays(1)));
    Seat seat =
        seats.save(new Seat(concert, "A", 1, "A-1", "VIP", BigDecimal.valueOf(50_000)));
    reservations.save(new Reservation(buyer, concert, seat));

    LocalDate today = LocalDate.now();
    mvc.perform(
            get("/api/analytics/sales").param("channelId", channel.getId().toString())
                .header("Authorization", bearer(manager))
                .param("from", today.toString())
                .param("to", today.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.channelId").value(channel.getId()))
        .andExpect(jsonPath("$.data.channelName").value(channel.getName()))
        .andExpect(jsonPath("$.data.totalOrderCount").value(2))
        .andExpect(jsonPath("$.data.totalQuantitySold").value(3))
        .andExpect(jsonPath("$.data.totalProductSalesAmount").value(30_000))
        .andExpect(jsonPath("$.data.totalReservationCount").value(1))
        .andExpect(jsonPath("$.data.totalConcertSalesAmount").value(50_000))
        .andExpect(jsonPath("$.data.totalSalesAmount").value(80_000))
        .andExpect(jsonPath("$.data.dailySales.length()").value(1))
        .andExpect(jsonPath("$.data.dailySales[0].orderCount").value(2))
        .andExpect(jsonPath("$.data.dailySales[0].reservationCount").value(1));

    mvc.perform(
            get("/api/analytics/sales/products").param("channelId", channel.getId().toString())
                .header("Authorization", bearer(manager)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].productId").value(product.getId()))
        .andExpect(jsonPath("$.data[0].quantitySold").value(3))
        .andExpect(jsonPath("$.data[0].salesAmount").value(30_000));

    mvc.perform(
            get("/api/analytics/sales/concerts").param("channelId", channel.getId().toString())
                .header("Authorization", bearer(manager)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].concertId").value(concert.getId()))
        .andExpect(jsonPath("$.data[0].reservationCount").value(1))
        .andExpect(jsonPath("$.data[0].salesAmount").value(50_000));

    mvc.perform(
            get("/api/analytics/seat-grades")
                .param("channelId", channel.getId().toString())
                .param("concertId", concert.getId().toString())
                .header("Authorization", bearer(manager)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].grade").value("VIP"))
        .andExpect(jsonPath("$.data[0].totalSeatCount").value(1))
        .andExpect(jsonPath("$.data[0].reservedSeatCount").value(1))
        .andExpect(jsonPath("$.data[0].reservationRate").value(100.0))
        .andExpect(jsonPath("$.data[0].salesAmount").value(50_000));
  }

  @Test
  void omittedDatesDefaultToRequestDate() throws Exception {
    String suffix = Long.toString(System.nanoTime());
    User manager = user("default_date_" + suffix, UserRole.MANAGER);
    Channel channel = channels.save(new Channel("DEFAULT DATE " + suffix, "sales test", null));
    channelUsers.save(new ChannelUser(channel, manager));
    LocalDate today = LocalDate.now();

    mvc.perform(
            get("/api/analytics/sales").param("channelId", channel.getId().toString())
                .header("Authorization", bearer(manager)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.from").value(today.toString()))
        .andExpect(jsonPath("$.data.to").value(today.toString()))
        .andExpect(jsonPath("$.data.totalSalesAmount").value(0));
  }

  @Test
  void managerCannotViewUnassignedChannelSales() throws Exception {
    String suffix = Long.toString(System.nanoTime());
    User manager = user("sales_unassigned_" + suffix, UserRole.MANAGER);
    Channel channel = channels.save(new Channel("UNASSIGNED CHANNEL " + suffix, "sales test", null));
    LocalDate today = LocalDate.now();

    mvc.perform(
            get("/api/analytics/sales").param("channelId", channel.getId().toString())
                .header("Authorization", bearer(manager))
                .param("from", today.toString())
                .param("to", today.toString()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("NOT_MANAGER"));
  }

  @Test
  void ordinaryUserCannotAccessSalesAnalytics() throws Exception {
    String suffix = Long.toString(System.nanoTime());
    User user = user("sales_user_" + suffix, UserRole.USER);
    Channel channel = channels.save(new Channel("BLOCKED CHANNEL " + suffix, "sales test", null));
    LocalDate today = LocalDate.now();

    mvc.perform(
            get("/api/analytics/sales").param("channelId", channel.getId().toString())
                .header("Authorization", bearer(user))
                .param("from", today.toString())
                .param("to", today.toString()))
        .andExpect(status().isForbidden());
  }

  private void order(User buyer, Product product, int quantity, boolean cancelled) {
    BigDecimal unitPrice = product.getPrice();
    Order order = orders.save(new Order(buyer, unitPrice.multiply(BigDecimal.valueOf(quantity))));
    orderItems.save(new OrderItem(order, product, unitPrice, quantity));
    if (cancelled) {
      order.cancel();
      orders.save(order);
    }
  }

  private User user(String name, UserRole role) {
    String nickname = name.substring(0, Math.min(name.length(), 30));
    User user = new User(name + "@test.com", "encoded-password", nickname);
    user.changeRole(role);
    return users.save(user);
  }

  private String bearer(User user) {
    return "Bearer " + tokens.createAccessToken(user);
  }
}
