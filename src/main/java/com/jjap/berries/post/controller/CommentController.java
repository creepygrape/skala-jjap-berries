package com.jjap.berries.post.controller;

import com.jjap.berries.global.common.response.ApiResponse;
import com.jjap.berries.global.security.CurrentUserId;
import com.jjap.berries.post.dto.CommentRequest;
import com.jjap.berries.post.dto.CommentResponse;
import com.jjap.berries.post.dto.CommentUpdateResponse;
import com.jjap.berries.post.dto.ReplyResponse;
import com.jjap.berries.post.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
  private final CommentService comments;

  @Operation(summary = "댓글과 답글 목록 조회", description = "postId의 최상위 댓글과 답글을 작성 순서로 조회합니다.")
  @GetMapping
  public ApiResponse<List<CommentResponse>> list(@RequestParam Long postId) {
    return ApiResponse.success(comments.list(postId), "댓글 목록입니다.");
  }

  @Operation(summary = "댓글 작성", description = "채널 가입 USER, 소속 ARTIST 또는 담당 MANAGER가 댓글을 작성합니다.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<CommentResponse> create(
      @CurrentUserId Long userId,
      @RequestParam Long postId,
      @Valid @RequestBody CommentRequest request) {
    return ApiResponse.success(comments.create(userId, postId, request), "댓글을 작성했습니다.");
  }

  @Operation(summary = "답글 작성", description = "댓글 또는 답글 ID를 대상으로 같은 댓글 그룹에 답글을 작성합니다.")
  @PostMapping("/{commentId}/replies")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<ReplyResponse> reply(
      @CurrentUserId Long userId,
      @PathVariable Long commentId,
      @Valid @RequestBody CommentRequest request) {
    return ApiResponse.success(comments.reply(userId, commentId, request), "답글을 작성했습니다.");
  }

  @Operation(summary = "댓글 또는 답글 수정", description = "작성자만 자신의 댓글이나 답글 내용을 수정할 수 있습니다.")
  @PatchMapping("/{commentId}")
  public ApiResponse<CommentUpdateResponse> update(
      @CurrentUserId Long userId,
      @PathVariable Long commentId,
      @Valid @RequestBody CommentRequest request) {
    return ApiResponse.success(comments.update(userId, commentId, request), "댓글을 수정했습니다.");
  }

  @Operation(summary = "댓글 또는 답글 삭제", description = "작성자 또는 담당 매니저가 댓글을 소프트 삭제하며 답글은 보존합니다.")
  @DeleteMapping("/{commentId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@CurrentUserId Long userId, @PathVariable Long commentId) {
    comments.delete(userId, commentId);
  }
}
