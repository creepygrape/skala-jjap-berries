package com.jjap.berries.artist.domain;

import com.jjap.berries.global.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Artist extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 2_000)
    private String description;

    @Column(length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ArtistStatus status;

    public Artist(String name, String description, String profileImageUrl) {
        this.name = name;
        this.description = description;
        this.profileImageUrl = profileImageUrl;
        this.status = ArtistStatus.ACTIVE;
    }
}
