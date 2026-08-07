package com.jjap.berries.concert.domain;

import com.jjap.berries.global.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"concert_id", "section", "seat_number"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "concert_id", nullable = false)
    private Concert concert;

    @Column(nullable = false, length = 50)
    private String section;

    @Column(name = "seat_number", nullable = false, length = 50)
    private String seatNumber;

    @Column(nullable = false, length = 50)
    private String grade;

    @Column(nullable = false, precision = 19, scale = 0)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SeatStatus status;

    public Seat(Concert concert, String section, String seatNumber, String grade, BigDecimal price) {
        this.concert = concert;
        this.section = section;
        this.seatNumber = seatNumber;
        this.grade = grade;
        this.price = price;
        this.status = SeatStatus.AVAILABLE;
    }

    public void reserve() {
        if (status != SeatStatus.AVAILABLE) {
            throw new IllegalStateException("예매 가능한 좌석이 아닙니다.");
        }
        this.status = SeatStatus.RESERVED;
    }

    public void makeAvailable() {
        this.status = SeatStatus.AVAILABLE;
    }
}
