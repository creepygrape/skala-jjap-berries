package com.jjap.berries.artist.domain;

import com.jjap.berries.global.common.domain.BaseEntity;
import com.jjap.berries.user.domain.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"artist_id", "user_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArtistManager extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public ArtistManager(Artist artist, User user) {
        this.artist = artist;
        this.user = user;
    }
}
