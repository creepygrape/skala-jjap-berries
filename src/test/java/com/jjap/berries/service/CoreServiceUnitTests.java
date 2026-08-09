package com.jjap.berries.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jjap.berries.channel.domain.Channel;
import com.jjap.berries.channel.repository.ChannelRepository;
import com.jjap.berries.community.repository.FanMembershipRepository;
import com.jjap.berries.concert.domain.Concert;
import com.jjap.berries.concert.dto.ConcertCreateRequest;
import com.jjap.berries.concert.dto.SeatCreateRequest;
import com.jjap.berries.concert.repository.ConcertRepository;
import com.jjap.berries.concert.repository.SeatRepository;
import com.jjap.berries.concert.service.ConcertService;
import com.jjap.berries.concert.service.SeatService;
import com.jjap.berries.concert.mapper.SeatQueryMapper;
import com.jjap.berries.global.exception.BusinessException;
import com.jjap.berries.global.exception.ErrorCode;
import com.jjap.berries.global.service.AccessService;
import com.jjap.berries.order.dto.OrderCreateRequest;
import com.jjap.berries.order.dto.OrderItemRequest;
import com.jjap.berries.order.repository.OrderItemRepository;
import com.jjap.berries.order.repository.OrderRepository;
import com.jjap.berries.order.service.OrderService;
import com.jjap.berries.post.dto.PostCreateRequest;
import com.jjap.berries.post.repository.PostRepository;
import com.jjap.berries.post.service.PostLikeService;
import com.jjap.berries.post.service.PostService;
import com.jjap.berries.product.repository.ProductRepository;
import com.jjap.berries.reservation.dto.ReservationCreateRequest;
import com.jjap.berries.reservation.repository.ReservationRepository;
import com.jjap.berries.reservation.service.ReservationService;
import com.jjap.berries.user.domain.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CoreServiceUnitTests {

  @Test
  void orderRejectsDuplicateProducts() {
    OrderRepository orders = mock(OrderRepository.class);
    OrderItemRepository items = mock(OrderItemRepository.class);
    ProductRepository products = mock(ProductRepository.class);
    AccessService access = mock(AccessService.class);
    OrderService service = new OrderService(orders, items, products, access);
    when(access.user(1L)).thenReturn(new User("order@test.com", "encoded", "order_user"));
    OrderCreateRequest request =
        new OrderCreateRequest(List.of(new OrderItemRequest(10L, 1), new OrderItemRequest(10L, 2)));

    assertError(ErrorCode.INVALID_REQUEST, () -> service.create(1L, request));
  }

  @Test
  void reservationRejectsRequestBeforeOpeningTime() {
    ReservationRepository reservations = mock(ReservationRepository.class);
    ConcertRepository concerts = mock(ConcertRepository.class);
    SeatRepository seats = mock(SeatRepository.class);
    AccessService access = mock(AccessService.class);
    ReservationService service = new ReservationService(reservations, concerts, seats, access);
    User user = new User("reservation@test.com", "encoded", "reservation_user");
    Channel artist = new Channel("RESERVATION UNIT", "test", null);
    LocalDateTime now = LocalDateTime.now();
    Concert concert =
        new Concert(artist, "공연", "공연장", now.plusDays(3), now.plusDays(1), now.plusDays(2));
    when(access.user(1L)).thenReturn(user);
    when(concerts.findById(10L)).thenReturn(Optional.of(concert));

    assertError(
        ErrorCode.RESERVATION_NOT_OPEN,
        () -> service.create(1L, 10L, new ReservationCreateRequest(20L)));
  }

  @Test
  void fanPostRequiresCommunityMembership() {
    PostRepository posts = mock(PostRepository.class);
    ChannelRepository artists = mock(ChannelRepository.class);
    FanMembershipRepository memberships = mock(FanMembershipRepository.class);
    PostLikeService postLikes = mock(PostLikeService.class);
    AccessService access = mock(AccessService.class);
    PostService service = new PostService(posts, artists, memberships, postLikes, access);
    when(access.user(1L)).thenReturn(new User("post@test.com", "encoded", "post_user"));
    when(memberships.existsByChannelIdAndUserId(10L, null)).thenReturn(false);

    assertError(
        ErrorCode.NOT_COMMUNITY_MEMBER,
        () -> service.create(1L, 10L, new PostCreateRequest("제목", "내용")));
  }

  @Test
  void concertRejectsInvalidReservationDateOrder() {
    ConcertRepository concerts = mock(ConcertRepository.class);
    ChannelRepository artists = mock(ChannelRepository.class);
    AccessService access = mock(AccessService.class);
    ConcertService service = new ConcertService(concerts, artists, access);
    when(access.user(1L)).thenReturn(new User("concert@test.com", "encoded", "concert_user"));
    LocalDateTime now = LocalDateTime.now();
    ConcertCreateRequest request =
        new ConcertCreateRequest("공연", "공연장", now.plusDays(3), now.plusDays(2), now.plusDays(1));

    assertError(ErrorCode.INVALID_REQUEST, () -> service.create(1L, 10L, request));
  }

  @Test
  void concertRejectsReservationStartThatIsNotInFuture() {
    ConcertRepository concerts = mock(ConcertRepository.class);
    ChannelRepository channels = mock(ChannelRepository.class);
    AccessService access = mock(AccessService.class);
    ConcertService service = new ConcertService(concerts, channels, access);
    LocalDateTime now = LocalDateTime.now();
    ConcertCreateRequest request =
        new ConcertCreateRequest(
            "공연", "공연장", now.plusDays(2), now.minusMinutes(1), now.plusDays(1));

    assertError(ErrorCode.INVALID_REQUEST, () -> service.create(1L, 10L, request));
  }

  @Test
  void seatRejectsDuplicateNumberInConcert() {
    SeatRepository seats = mock(SeatRepository.class);
    ConcertRepository concerts = mock(ConcertRepository.class);
    AccessService access = mock(AccessService.class);
    ReservationRepository reservations = mock(ReservationRepository.class);
    SeatQueryMapper seatQueries = mock(SeatQueryMapper.class);
    SeatService service = new SeatService(seats, concerts, access, reservations, seatQueries);
    Channel artist = new Channel("SEAT UNIT", "test", null);
    LocalDateTime now = LocalDateTime.now();
    Concert concert =
        new Concert(artist, "공연", "공연장", now.plusDays(3), now.plusDays(1), now.plusDays(2));
    when(access.user(1L)).thenReturn(new User("seat@test.com", "encoded", "seat_user"));
    when(concerts.findByIdForUpdate(10L)).thenReturn(Optional.of(concert));
    when(seats.existsByConcertIdAndSectionAndSeatSequence(10L, "A", 1)).thenReturn(true);

    assertError(
        ErrorCode.SEAT_ALREADY_EXISTS,
        () ->
            service.create(
                1L, 10L, new SeatCreateRequest("A", 1, "A-1", "VIP", BigDecimal.TEN)));
  }

  private void assertError(ErrorCode expected, ThrowingRunnable action) {
    assertThatThrownBy(action::run)
        .isInstanceOfSatisfying(
            BusinessException.class,
            exception -> assertThat(exception.getErrorCode()).isEqualTo(expected));
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run();
  }
}
