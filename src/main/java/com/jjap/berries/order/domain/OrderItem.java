package com.jjap.berries.order.domain;

import com.jjap.berries.global.common.domain.BaseEntity;
import com.jjap.berries.product.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 19, scale = 0)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    public OrderItem(Order order, Product product, BigDecimal unitPrice, int quantity) {
        this.order = order;
        this.product = product;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public BigDecimal calculatePrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
