package com.jjap.berries.post.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
    Long commentId,
    String authorNickname,
    String content,
    boolean deleted,
    LocalDateTime createdAt,
    List<ReplyResponse> replies) {}
