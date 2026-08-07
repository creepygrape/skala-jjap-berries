package com.jjap.berries.product.domain;

import com.jjap.berries.artist.domain.Artist;
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
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

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

    public Product(Artist artist, String name, String description, BigDecimal price, int stock, String imageUrl) {
        this.artist = artist;
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
}
