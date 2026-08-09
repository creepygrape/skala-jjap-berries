package com.jjap.berries.product.domain;

import com.jjap.berries.channel.domain.Channel;
import com.jjap.berries.global.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

  @ManyToOne(optional = false)
  @JoinColumn(name = "channel_id", nullable = false)
  private Channel channel;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(nullable = false, length = 2_000)
  private String description;

  @Column(nullable = false, precision = 19, scale = 0)
  private BigDecimal price;

  @Column(nullable = false)
  private int stock;

  @Column(length = 500)
  private String imageUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private ProductStatus status;

  public Product(
      Channel channel,
      String name,
      String description,
      BigDecimal price,
      int stock,
      String imageUrl) {
    this.channel = channel;
    this.name = name;
    this.description = description;
    this.price = price;
    this.stock = stock;
    this.imageUrl = imageUrl;
    this.status = ProductStatus.ON_SALE;
  }

  public void decreaseStock(int quantity) {
    if (stock < quantity) {
      throw new IllegalStateException("재고가 부족합니다.");
    }
    stock -= quantity;
  }

  public void restoreStock(int quantity) {
    stock += quantity;
  }

  public void update(
      String name, String description, BigDecimal price, Integer stock, String imageUrl) {
    if (name != null) {
      this.name = name;
    }
    if (description != null) {
      this.description = description;
    }
    if (price != null) {
      this.price = price;
    }
    if (stock != null) {
      this.stock = stock;
    }
    if (imageUrl != null) {
      this.imageUrl = imageUrl;
    }
  }

  public void changeStatus(ProductStatus status) {
    this.status = status;
  }
}
