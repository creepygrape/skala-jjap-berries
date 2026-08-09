package com.jjap.berries.post.dto;

import com.jjap.berries.post.domain.Comment;
import java.time.LocalDateTime;

public record CommentUpdateResponse(
    Long commentId, String authorNickname, String content, LocalDateTime createdAt) {

  public static CommentUpdateResponse from(Comment comment) {
    return new CommentUpdateResponse(
        comment.getId(), comment.getAuthor().getNickname(), comment.getContent(), comment.getCreatedAt());
  }
}
