package com.jjap.berries.post.domain;

import com.jjap.berries.channel.domain.Channel;
import com.jjap.berries.global.common.domain.BaseEntity;
import com.jjap.berries.user.domain.User;
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
public class Post extends BaseEntity {

  @ManyToOne(optional = false)
  @JoinColumn(name = "channel_id", nullable = false)
  private Channel channel;

  // 탈퇴 사용자 정리 시 null이 되며, 조회에서는 '탈퇴한 사용자'로 표시한다.
  @ManyToOne
  @JoinColumn(name = "author_id")
  private User author;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, length = 10_000)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private PostType type;

  private LocalDateTime deletedAt;

  public Post(Channel channel, User author, String title, String content, PostType type) {
    this.channel = channel;
    this.author = author;
    this.title = title;
    this.content = content;
    this.type = type;
  }

  public void delete() {
    this.deletedAt = LocalDateTime.now();
  }

  public void update(String title, String content) {
    if (title != null) {
      this.title = title;
    }
    if (content != null) {
      this.content = content;
    }
  }
}
