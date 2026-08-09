package com.jjap.berries.global;

import static org.assertj.core.api.Assertions.assertThat;

import com.jjap.berries.concert.dto.ConcertCreateRequest;
import com.jjap.berries.concert.dto.ConcertUpdateRequest;
import com.jjap.berries.concert.dto.SeatBulkRequest;
import com.jjap.berries.concert.dto.SeatCreateRequest;
import com.jjap.berries.concert.dto.SeatUpdateRequest;
import com.jjap.berries.order.dto.OrderCreateRequest;
import com.jjap.berries.order.dto.OrderItemRequest;
import com.jjap.berries.post.dto.PostCreateRequest;
import com.jjap.berries.product.dto.ProductCreateRequest;
import com.jjap.berries.product.dto.ProductUpdateRequest;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RequestValidationTests {
  @Autowired Validator validator;

  @Test
  void rejectsInvalidCreateRequests() {
    assertThat(
            validator.validate(
                new ProductCreateRequest(" ", "description", BigDecimal.valueOf(-1), -1, null)))
        .isNotEmpty();
    assertThat(
            validator.validate(
                new ConcertCreateRequest(
                    "title", "venue", null, LocalDateTime.now(), LocalDateTime.now())))
        .isNotEmpty();
    assertThat(
            validator.validate(
                new SeatCreateRequest("A", 1, "A-1", "VIP", BigDecimal.valueOf(-1))))
        .isNotEmpty();
    assertThat(validator.validate(new PostCreateRequest("title", " "))).isNotEmpty();
  }

  @Test
  void validatesNestedCollectionItems() {
    OrderCreateRequest order = new OrderCreateRequest(List.of(new OrderItemRequest(null, 0)));
    SeatBulkRequest seats = new SeatBulkRequest("", 1, 501, "VIP", BigDecimal.ZERO);
    assertThat(validator.validate(order)).isNotEmpty();
    assertThat(validator.validate(seats)).isNotEmpty();
  }

  @Test
  void patchAllowsNullButRejectsBlankAndOversizedValues() {
    assertThat(validator.validate(new ProductUpdateRequest(null, null, null, null, null)))
        .isEmpty();
    assertThat(validator.validate(new ProductUpdateRequest(" ", null, null, null, null)))
        .isNotEmpty();
    assertThat(
            validator.validate(new ConcertUpdateRequest("x".repeat(201), null, null, null, null)))
        .isNotEmpty();
    assertThat(
            validator.validate(
                new SeatUpdateRequest(null, null, "x".repeat(51), null, null, null)))
        .isNotEmpty();
  }
}
