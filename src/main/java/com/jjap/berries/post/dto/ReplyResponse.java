package com.jjap.berries.post.dto;

import java.time.LocalDateTime;

public record ReplyResponse(
    Long commentId,
    String authorNickname,
    Long replyToCommentId,
    String replyToNickname,
    String content,
    boolean deleted,
    LocalDateTime createdAt) {}
