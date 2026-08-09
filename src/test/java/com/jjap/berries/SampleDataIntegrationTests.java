package com.jjap.berries;

import static org.assertj.core.api.Assertions.assertThat;

import com.jjap.berries.auth.dto.LoginRequest;
import com.jjap.berries.auth.dto.TokenResponse;
import com.jjap.berries.auth.service.AuthService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class SampleDataIntegrationTests {

  private static final Pattern TIMESTAMP_PATTERN =
      Pattern.compile("TIMESTAMP '(2025|2026)-(\\d{2})-(\\d{2}) ");

  @Autowired JdbcTemplate jdbcTemplate;
  @Autowired AuthService authService;

  @Test
  @Sql("classpath:data.sql")
  void everyTableHasAtLeastTenRowsAndEverySamplePasswordIs1234() {
    List<String> tables =
        List.of(
            "users",
            "channel",
            "channel_user",
            "fan_membership",
            "concert",
            "seat",
            "reservation",
            "product",
            "orders",
            "order_item",
            "post",
            "comment",
            "post_like",
            "refresh_token",
            "revoked_access_token");

    for (String table : tables) {
      Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
      assertThat(count).as("row count of %s", table).isGreaterThanOrEqualTo(10);
    }

    List<String> emails =
        jdbcTemplate.queryForList(
            "SELECT email FROM users WHERE id BETWEEN 1001 AND 1030 ORDER BY id", String.class);
    assertThat(emails).hasSize(30);
    for (String email : emails) {
      TokenResponse tokens = authService.login(new LoginRequest(email, "1234"));
      assertThat(tokens.accessToken()).isNotBlank();
      assertThat(tokens.refreshToken()).isNotBlank();
    }

    assertGroupedRowCount(
        "SELECT channel_id FROM concert GROUP BY channel_id HAVING COUNT(*) >= 3", 10);
    assertGroupedRowCount(
        "SELECT channel_id FROM product GROUP BY channel_id HAVING COUNT(*) >= 3", 10);
    assertGroupedRowCount(
        "SELECT concert_id FROM seat GROUP BY concert_id HAVING COUNT(DISTINCT grade) >= 3", 30);
    assertGroupedRowCount(
        "SELECT p.channel_id FROM orders o "
            + "JOIN order_item oi ON oi.order_id = o.id "
            + "JOIN product p ON p.id = oi.product_id "
            + "WHERE o.status <> 'CANCELLED' GROUP BY p.channel_id "
            + "HAVING COUNT(DISTINCT p.id) >= 3",
        10);
    assertGroupedRowCount(
        "SELECT c.channel_id FROM reservation r "
            + "JOIN concert c ON c.id = r.concert_id "
            + "WHERE r.status <> 'CANCELLED' GROUP BY c.channel_id "
            + "HAVING COUNT(DISTINCT c.id) >= 2 AND COUNT(DISTINCT CAST(r.reserved_at AS DATE)) >= 2",
        10);
  }

  @Test
  void sampleSqlUsesOnlyFixedDatesWithinTheRequestedRange() throws IOException {
    String sql;
    try (var input = getClass().getResourceAsStream("/data.sql")) {
      assertThat(input).isNotNull();
      sql = new String(input.readAllBytes(), StandardCharsets.UTF_8);
    }

    assertThat(sql).doesNotContainIgnoringCase("CURRENT_TIMESTAMP");

    Matcher matcher = TIMESTAMP_PATTERN.matcher(sql);
    Set<Integer> years = new HashSet<>();
    Set<Integer> months = new HashSet<>();
    int timestampCount = 0;
    while (matcher.find()) {
      timestampCount++;
      years.add(Integer.parseInt(matcher.group(1)));
      int month = Integer.parseInt(matcher.group(2));
      int day = Integer.parseInt(matcher.group(3));
      assertThat(month).isBetween(1, 12);
      assertThat(day).isBetween(1, 25);
      months.add(month);
    }

    assertThat(timestampCount).isPositive();
    assertThat(years).containsExactlyInAnyOrder(2025, 2026);
    assertThat(months).containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
  }

  private void assertGroupedRowCount(String sql, int expected) {
    assertThat(jdbcTemplate.queryForList(sql)).hasSize(expected);
  }
}
