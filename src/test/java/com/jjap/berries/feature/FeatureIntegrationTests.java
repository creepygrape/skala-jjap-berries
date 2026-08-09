package com.jjap.berries.feature;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jjap.berries.channel.domain.Channel;
import com.jjap.berries.channel.domain.ChannelUser;
import com.jjap.berries.channel.repository.ChannelRepository;
import com.jjap.berries.channel.repository.ChannelUserRepository;
import com.jjap.berries.community.domain.FanMembership;
import com.jjap.berries.community.repository.FanMembershipRepository;
import com.jjap.berries.concert.domain.Concert;
import com.jjap.berries.concert.domain.Seat;
import com.jjap.berries.concert.domain.SeatStatus;
import com.jjap.berries.concert.repository.ConcertRepository;
import com.jjap.berries.concert.repository.SeatRepository;
import com.jjap.berries.global.security.JwtTokenProvider;
import com.jjap.berries.product.domain.Product;
import com.jjap.berries.product.repository.ProductRepository;
import com.jjap.berries.user.domain.User;
import com.jjap.berries.user.domain.UserRole;
import com.jjap.berries.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class FeatureIntegrationTests {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;
  @Autowired JwtTokenProvider tokens;
  @Autowired UserRepository users;
  @Autowired ChannelRepository channels;
  @Autowired ChannelUserRepository channelUsers;
  @Autowired FanMembershipRepository memberships;
  @Autowired ProductRepository products;
  @Autowired ConcertRepository concerts;
  @Autowired SeatRepository seats;

  @Test
  void channelDetailContainsOnlyArtistMembers() throws Exception {
    User artist = user("detail_artist", UserRole.ARTIST);
    User manager = user("detail_manager", UserRole.MANAGER);
    Channel channel = channels.save(new Channel("DETAIL CHANNEL", "channel detail test", null));
    channelUsers.save(new ChannelUser(channel, artist));
    channelUsers.save(new ChannelUser(channel, manager));

    mvc.perform(get("/api/channels/{channelId}", channel.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.channelId").value(channel.getId()))
        .andExpect(jsonPath("$.data.artists.length()").value(1))
        .andExpect(jsonPath("$.data.artists[0].artistId").value(artist.getId()))
        .andExpect(jsonPath("$.data.artists[0].nickname").value(artist.getNickname()));
  }

  @Test
  void managerCanViewOnlyUsersWhoJoinedManagedChannel() throws Exception {
    String suffix = Long.toString(System.nanoTime());
    User manager = user("fan_manager_" + suffix, UserRole.MANAGER);
    User fan = user("joined_fan_" + suffix, UserRole.USER);
    User artist = user("joined_artist_" + suffix, UserRole.ARTIST);
    Channel channel =
        channels.save(new Channel("FAN LIST CHANNEL " + suffix, "fan list test", null));
    channelUsers.save(new ChannelUser(channel, manager));
    memberships.save(new FanMembership(channel, fan));
    memberships.save(new FanMembership(channel, artist));

    mvc.perform(
            get("/api/channels/{channelId}/members", channel.getId())
                .header("Authorization", bearer(manager)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].userId").value(fan.getId()))
        .andExpect(jsonPath("$.data[0].email").value(fan.getEmail()))
        .andExpect(jsonPath("$.data[0].nickname").value(fan.getNickname()))
        .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
        .andExpect(jsonPath("$.data[0].joinedAt").exists());
  }

  @Test
  void managerCannotViewUsersOfUnmanagedChannel() throws Exception {
    String suffix = Long.toString(System.nanoTime());
    User manager = user("other_manager_" + suffix, UserRole.MANAGER);
    Channel channel =
        channels.save(new Channel("OTHER FAN CHANNEL " + suffix, "access test", null));

    mvc.perform(
            get("/api/channels/{channelId}/members", channel.getId())
                .header("Authorization", bearer(manager)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("NOT_MANAGER"));
  }

  @Test
  void addingExistingArtistToSameChannelReturnsSpecificMessage() throws Exception {
    String suffix = Long.toString(System.nanoTime());
    User artist = user("sca_" + suffix, UserRole.ARTIST);
    User manager = user("scm_" + suffix, UserRole.MANAGER);
    Channel channel =
        channels.save(new Channel("SAME CHANNEL " + suffix, "duplicate member test", null));
    channelUsers.save(new ChannelUser(channel, artist));
    channelUsers.save(new ChannelUser(channel, manager));

    mvc.perform(
            post("/api/channels/{channelId}/members", channel.getId())
                .header("Authorization", bearer(manager))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(new MemberBody(artist.getId()))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("SAME_CHANNEL_USER_ALREADY_EXISTS"))
        .andExpect(jsonPath("$.message").value("이미 해당 채널에 소속된 사용자입니다."));
  }

  @Test
  void addingArtistToMissingChannelReturnsNotFoundBeforeAccessDenied() throws Exception {
    String suffix = Long.toString(System.nanoTime());
    User artist = user("missing_artist_" + suffix, UserRole.ARTIST);
    User manager = user("missing_manager_" + suffix, UserRole.MANAGER);

    mvc.perform(
            post("/api/channels/{channelId}/members", Long.MAX_VALUE)
                .header("Authorization", bearer(manager))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new MemberBody(artist.getId()))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"));
  }

  @Test
  void addingArtistToUnassignedChannelReturnsForbidden() throws Exception {
    String suffix = Long.toString(System.nanoTime());
    User artist = user("ua_" + suffix, UserRole.ARTIST);
    User manager = user("um_" + suffix, UserRole.MANAGER);
    Channel channel =
        channels.save(new Channel("UNASSIGNED MEMBER CHANNEL " + suffix, "access test", null));

    mvc.perform(
            post("/api/channels/{channelId}/members", channel.getId())
                .header("Authorization", bearer(manager))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new MemberBody(artist.getId()))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("NOT_MANAGER"));
  }

  @Test
  void removedArtistCanBeAddedToSameChannelAgain() throws Exception {
    String suffix = Long.toString(System.nanoTime());
    User artist = user("ra_" + suffix, UserRole.ARTIST);
    User manager = user("rm_" + suffix, UserRole.MANAGER);
    Channel channel =
        channels.save(new Channel("REASSIGN CHANNEL " + suffix, "reassign test", null));
    channelUsers.save(new ChannelUser(channel, manager));
    channelUsers.save(new ChannelUser(channel, artist));
    MemberBody request = new MemberBody(artist.getId());

    mvc.perform(
            delete("/api/channels/{channelId}/members", channel.getId())
                .header("Authorization", bearer(manager))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    mvc.perform(
            post("/api/channels/{channelId}/members", channel.getId())
                .header("Authorization", bearer(manager))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }

  @Test
  void managerCannotRemoveOwnChannelManagerRole() throws Exception {
    String suffix = Long.toString(System.nanoTime());
    User manager = user("self_mgr_" + suffix, UserRole.MANAGER);
    Channel channel = channels.save(new Channel("SELF MANAGER " + suffix, "test", null));
    channelUsers.save(new ChannelUser(channel, manager));

    mvc.perform(
            delete(
                    "/api/channels/{channelId}/managers/{managerId}",
                    channel.getId(),
                    manager.getId())
                .header("Authorization", bearer(manager)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("CANNOT_REMOVE_SELF"));
  }

  @Test
  void communityPostCommentAndLikeFlow() throws Exception {
    User fan = user("feature_fan", UserRole.USER);
    User outsider = user("feature_outsider", UserRole.USER);
    User artistUser = user("feature_artist_user", UserRole.ARTIST);
    User manager = user("feature_comment_manager", UserRole.MANAGER);
    Channel channel = channels.save(new Channel("FEATURE CHANNEL", "integration test", null));
    channelUsers.save(new ChannelUser(channel, artistUser));
    channelUsers.save(new ChannelUser(channel, manager));
    memberships.save(new FanMembership(channel, fan));

    mvc.perform(
            post("/api/posts").param("channelId", channel.getId().toString())
                .header("Authorization", bearer(outsider))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"title":"권한 없는 게시글","content":"게시글 내용"}
                    """))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("NOT_COMMUNITY_MEMBER"));

    long postId =
        dataId(
            mvc.perform(
                    post("/api/posts").param("channelId", channel.getId().toString())
                        .header("Authorization", bearer(fan))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                            {"title":"통합 테스트 게시글","content":"게시글 내용"}
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.authorNickname").value(fan.getNickname()))
                .andExpect(jsonPath("$.data.type").value("FAN"))
                .andReturn()
                .getResponse()
                .getContentAsString(),
            "postId");

    mvc.perform(
            post("/api/comments").param("postId", Long.toString(postId))
                .header("Authorization", bearer(fan))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"댓글 내용\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.content").value("댓글 내용"));

    mvc.perform(
            post("/api/comments").param("postId", Long.toString(postId))
                .header("Authorization", bearer(artistUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"아티스트 댓글\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.authorNickname").value(artistUser.getNickname()));

    mvc.perform(
            post("/api/comments").param("postId", Long.toString(postId))
                .header("Authorization", bearer(manager))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"매니저 댓글\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.authorNickname").value(manager.getNickname()));

    mvc.perform(post("/api/posts/{postId}/likes", postId).header("Authorization", bearer(fan)))
        .andExpect(status().isCreated());

    mvc.perform(get("/api/posts/{postId}/likes", postId).header("Authorization", bearer(fan)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.likeCount").value(1))
        .andExpect(jsonPath("$.data.liked").value(true));

    mvc.perform(get("/api/comments").param("postId", Long.toString(postId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].content").value("댓글 내용"));
  }

  @Test
  void orderAndReservationFlow() throws Exception {
    User buyer = user("feature_buyer", UserRole.USER);
    Channel artist = channels.save(new Channel("FEATURE SHOP CHANNEL", "integration test", null));
    Product product =
        products.save(
            new Product(artist, "통합 테스트 상품", "상품 설명", BigDecimal.valueOf(10000), 2, null));

    mvc.perform(
            post("/api/orders")
                .header("Authorization", bearer(buyer))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"items\":[{\"productId\":" + product.getId() + ",\"quantity\":1}]}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.totalPrice").value(10000))
        .andExpect(jsonPath("$.data.items[0].quantity").value(1));

    LocalDateTime now = LocalDateTime.now();
    Concert concert =
        concerts.save(
            new Concert(
                artist,
                "통합 테스트 공연",
                "테스트 공연장",
                now.plusDays(2),
                now.minusDays(1),
                now.plusDays(1)));
    Seat seat =
        seats.save(new Seat(concert, "A", 99, "A-99", "VIP", BigDecimal.valueOf(120000)));

    mvc.perform(
            post("/api/reservations").param("concertId", concert.getId().toString())
                .header("Authorization", bearer(buyer))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"seatId\":" + seat.getId() + "}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.seatId").value(seat.getId()))
        .andExpect(jsonPath("$.data.status").value("RESERVED"));

    mvc.perform(get("/api/seats").param("concertId", concert.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[0].status").value(SeatStatus.RESERVED.name()));
  }

  @Test
  void managerBulkCreatesSequentialSeatsAndMarksSeatReserved() throws Exception {
    String suffix = Long.toString(System.nanoTime());
    User manager = user("seat_manager_" + suffix, UserRole.MANAGER);
    Channel channel = channels.save(new Channel("SEAT CHANNEL " + suffix, "seat test", null));
    channelUsers.save(new ChannelUser(channel, manager));
    LocalDateTime now = LocalDateTime.now();
    Concert concert =
        concerts.save(
            new Concert(
                channel,
                "BULK SEAT CONCERT " + suffix,
                "TEST VENUE",
                now.plusDays(3),
                now.plusDays(1),
                now.plusDays(2)));

    String response =
        mvc.perform(
                post("/api/seats/bulk").param("concertId", concert.getId().toString())
                    .header("Authorization", bearer(manager))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"section":"A","startNumber":5,"count":3,"grade":"VIP","price":120000}
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.startNumber").value(5))
            .andExpect(jsonPath("$.data.endNumber").value(7))
            .andExpect(jsonPath("$.data.createdCount").value(3))
            .andExpect(jsonPath("$.data.seats.length()").value(3))
            .andExpect(jsonPath("$.data.seats[0].seatSequence").value(5))
            .andExpect(jsonPath("$.data.seats[0].seatLabel").value("A-5"))
            .andExpect(jsonPath("$.data.seats[1].seatSequence").value(6))
            .andExpect(jsonPath("$.data.seats[2].seatSequence").value(7))
            .andExpect(jsonPath("$.data.seats[0].status").value("AVAILABLE"))
            .andReturn()
            .getResponse()
            .getContentAsString();

    long seatId =
        mapper.readTree(response).path("data").path("seats").get(0).path("seatId").asLong();
    mvc.perform(
            patch("/api/seats/{seatId}", seatId)
                .header("Authorization", bearer(manager))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"RESERVED\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("RESERVED"));

    mvc.perform(
            post("/api/seats/bulk").param("concertId", concert.getId().toString())
                .header("Authorization", bearer(manager))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"section":"A","startNumber":7,"count":2,"grade":"VIP","price":120000}
                    """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("SEAT_ALREADY_EXISTS"));

    mvc.perform(
            post("/api/seats/bulk").param("concertId", concert.getId().toString())
                .header("Authorization", bearer(manager))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"section":"A","count":2,"grade":"VIP","price":120000}
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.startNumber").value(8))
        .andExpect(jsonPath("$.data.endNumber").value(9))
        .andExpect(jsonPath("$.data.createdCount").value(2));

    mvc.perform(
            get("/api/seats").param("concertId", concert.getId().toString())
                .param("section", "A")
                .param("grade", "VIP")
                .param("status", "AVAILABLE")
                .param("page", "0")
                .param("size", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content.length()").value(1))
        .andExpect(jsonPath("$.data.content[0].seatSequence").value(6))
        .andExpect(jsonPath("$.data.page").value(0))
        .andExpect(jsonPath("$.data.size").value(1))
        .andExpect(jsonPath("$.data.totalElements").value(4))
        .andExpect(jsonPath("$.data.totalPages").value(4));
  }

  @Test
  void managerDeletesIndividualSeat() throws Exception {
    String suffix = Long.toString(System.nanoTime());
    User manager = user("dsm_" + suffix, UserRole.MANAGER);
    Channel channel = channels.save(new Channel("DELETE SEAT CHANNEL " + suffix, "test", null));
    channelUsers.save(new ChannelUser(channel, manager));
    LocalDateTime now = LocalDateTime.now();
    Concert concert =
        concerts.save(
            new Concert(
                channel,
                "DELETE SEAT CONCERT " + suffix,
                "TEST VENUE",
                now.plusDays(3),
                now.plusDays(1),
                now.plusDays(2)));
    Seat individual = seats.save(new Seat(concert, "A", 1, "A-1", "VIP", BigDecimal.TEN));
    mvc.perform(
            delete("/api/seats/{seatId}", individual.getId())
                .header("Authorization", bearer(manager)))
        .andExpect(status().isNoContent());

    mvc.perform(get("/api/seats").param("concertId", concert.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content.length()").value(0))
        .andExpect(jsonPath("$.data.totalElements").value(0));
  }

  @Test
  void managerCannotDeleteSeatsAfterReservationStarts() throws Exception {
    String suffix = Long.toString(System.nanoTime());
    User manager = user("dsm2_" + suffix, UserRole.MANAGER);
    Channel channel = channels.save(new Channel("CLOSED DELETE " + suffix, "test", null));
    channelUsers.save(new ChannelUser(channel, manager));
    LocalDateTime now = LocalDateTime.now();
    Concert concert =
        concerts.save(
            new Concert(
                channel,
                "CLOSED DELETE CONCERT " + suffix,
                "TEST VENUE",
                now.plusDays(3),
                now.minusMinutes(1),
                now.plusDays(2)));
    Seat seat = seats.save(new Seat(concert, "A", 1, "A-1", "VIP", BigDecimal.TEN));

    mvc.perform(
            delete("/api/seats/{seatId}", seat.getId())
                .header("Authorization", bearer(manager)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("SEAT_DELETION_CLOSED"));
  }

  @Test
  void managerCannotUpdateSeatAfterReservationStarts() throws Exception {
    String suffix = Long.toString(System.nanoTime());
    User manager = user("usm_" + suffix, UserRole.MANAGER);
    Channel channel = channels.save(new Channel("CLOSED UPDATE " + suffix, "test", null));
    channelUsers.save(new ChannelUser(channel, manager));
    LocalDateTime now = LocalDateTime.now();
    Concert concert =
        concerts.save(
            new Concert(
                channel,
                "CLOSED UPDATE CONCERT " + suffix,
                "TEST VENUE",
                now.plusDays(3),
                now.minusMinutes(1),
                now.plusDays(2)));
    Seat seat = seats.save(new Seat(concert, "A", 1, "A-1", "VIP", BigDecimal.TEN));

    mvc.perform(
            patch("/api/seats/{seatId}", seat.getId())
                .header("Authorization", bearer(manager))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"grade\":\"R\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("SEAT_UPDATE_CLOSED"));

    mvc.perform(
            post("/api/seats").param("concertId", concert.getId().toString())
                .header("Authorization", bearer(manager))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"section\":\"A\",\"seatSequence\":2,\"seatLabel\":\"A-2\",\"grade\":\"VIP\",\"price\":10000}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("SEAT_CREATION_CLOSED"));

    mvc.perform(
            post("/api/seats/bulk").param("concertId", concert.getId().toString())
                .header("Authorization", bearer(manager))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"section\":\"B\",\"count\":2,\"grade\":\"R\",\"price\":10000}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("SEAT_CREATION_CLOSED"));

    mvc.perform(
            patch("/api/concerts/{concertId}", concert.getId())
                .header("Authorization", bearer(manager))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"수정된 공연 제목\",\"venue\":\"수정된 장소\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.title").value("수정된 공연 제목"))
        .andExpect(jsonPath("$.data.venue").value("수정된 장소"));

    mvc.perform(
            patch("/api/concerts/{concertId}", concert.getId())
                .header("Authorization", bearer(manager))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"concertAt\":\"" + now.plusDays(4) + "\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("CONCERT_SCHEDULE_UPDATE_CLOSED"));
  }

  @Test
  void protectedApiRejectsUnauthenticatedRequest() throws Exception {
    mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }

  private User user(String name, UserRole role) {
    User user = users.save(new User(name + "@berries.com", "encoded", name));
    user.changeRole(role);
    return users.save(user);
  }

  private String bearer(User user) {
    return "Bearer " + tokens.createAccessToken(user);
  }

  private long dataId(String response, String field) throws Exception {
    JsonNode data = mapper.readTree(response).path("data");
    return data.path(field).asLong();
  }

  record MemberBody(Long artistId) {}
}
