package com.jjap.berries.post.domain;

import com.jjap.berries.global.common.domain.BaseEntity;
import com.jjap.berries.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

  @ManyToOne(optional = false)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  // 탈퇴 사용자 정리 시 null이 되며, 조회에서는 '탈퇴한 사용자'로 표시한다.
  @ManyToOne
  @JoinColumn(name = "author_id")
  private User author;

  @Column(nullable = false, length = 2_000)
  private String content;

  // 최상위 댓글이면 null, 답글이면 자신이 속한 최상위 댓글을 참조한다.
  @ManyToOne
  @JoinColumn(name = "root_comment_id")
  private Comment rootComment;

  // 답글의 실제 응답 대상. 최상위 댓글이면 null이다.
  @ManyToOne
  @JoinColumn(name = "reply_to_comment_id")
  private Comment replyToComment;

  private LocalDateTime deletedAt;

  private Comment(
      Post post, User author, String content, Comment rootComment, Comment replyToComment) {
    this.post = post;
    this.author = author;
    this.content = content;
    this.rootComment = rootComment;
    this.replyToComment = replyToComment;
  }

  public static Comment root(Post post, User author, String content) {
    return new Comment(post, author, content, null, null);
  }

  public static Comment reply(
      Post post, User author, String content, Comment rootComment, Comment replyToComment) {
    return new Comment(post, author, content, rootComment, replyToComment);
  }

  public void delete() {
    this.deletedAt = LocalDateTime.now();
  }

  public void update(String content) {
    this.content = content;
  }
}
