package com.jjap.berries.concert.domain;

import com.jjap.berries.artist.domain.Artist;
import com.jjap.berries.global.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Concert extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 300)
    private String venue;

    @Column(nullable = false)
    private LocalDateTime concertAt;

    @Column(nullable = false)
    private LocalDateTime reservationStartAt;

    @Column(nullable = false)
    private LocalDateTime reservationEndAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConcertStatus status;

    public Concert(Artist artist, String title, String venue, LocalDateTime concertAt,
                   LocalDateTime reservationStartAt, LocalDateTime reservationEndAt) {
        this.artist = artist;
        this.title = title;
        this.venue = venue;
        this.concertAt = concertAt;
        this.reservationStartAt = reservationStartAt;
        this.reservationEndAt = reservationEndAt;
        this.status = ConcertStatus.ON_SALE;
    }
}
