package com.jjap.berries.post.dto;

import com.jjap.berries.post.domain.Post;
import com.jjap.berries.post.domain.PostType;
import java.time.LocalDateTime;

public record PostResponse(
    Long postId,
    Long channelId,
    Long authorId,
    String authorNickname,
    String title,
    String content,
    PostType type,
    LocalDateTime createdAt,
    long likeCount) {
  public static PostResponse from(Post post, long likeCount) {
    return new PostResponse(
        post.getId(),
        post.getChannel().getId(),
        post.getAuthor() == null ? null : post.getAuthor().getId(),
        post.getAuthor() == null ? "탈퇴한 사용자" : post.getAuthor().getNickname(),
        post.getTitle(),
        post.getContent(),
        post.getType(),
        post.getCreatedAt(),
        likeCount);
  }
}
