package com.jjap.berries.post.controller;

import com.jjap.berries.global.common.response.ApiResponse;
import com.jjap.berries.global.security.CurrentUserId;
import com.jjap.berries.post.domain.PostType;
import com.jjap.berries.post.dto.LikeResponse;
import com.jjap.berries.post.dto.PostCreateRequest;
import com.jjap.berries.post.dto.PostResponse;
import com.jjap.berries.post.dto.PostUpdateRequest;
import com.jjap.berries.post.service.PostLikeService;
import com.jjap.berries.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
  private final PostService posts;
  private final PostLikeService postLikes;

  @Operation(summary = "게시글 목록 조회", description = "channelId와 선택 유형, 페이지 조건으로 삭제되지 않은 게시글을 조회합니다.")
  @GetMapping
  public ApiResponse<Page<PostResponse>> list(
      @RequestParam Long channelId,
      @RequestParam(required = false) PostType type,
      @ParameterObject @PageableDefault Pageable pageable) {
    return ApiResponse.success(posts.list(channelId, type, pageable), "게시글 목록입니다.");
  }

  @Operation(summary = "게시글 상세 조회", description = "게시글 ID로 작성자, 유형, 본문과 좋아요 수를 조회합니다.")
  @GetMapping("/{postId}")
  public ApiResponse<PostResponse> get(@PathVariable Long postId) {
    return ApiResponse.success(posts.get(postId), "게시글입니다.");
  }

  @Operation(summary = "게시글 작성", description = "채널 소속과 역할에 따라 FAN, ARTIST 또는 NOTICE 게시글을 작성합니다.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<PostResponse> create(
      @CurrentUserId Long userId,
      @RequestParam Long channelId,
      @Valid @RequestBody PostCreateRequest request) {
    return ApiResponse.success(posts.create(userId, channelId, request), "게시글을 작성했습니다.");
  }

  @Operation(summary = "게시글 수정", description = "작성자만 자신의 게시글 제목과 내용을 수정할 수 있습니다.")
  @PatchMapping("/{postId}")
  public ApiResponse<PostResponse> update(
      @CurrentUserId Long userId,
      @PathVariable Long postId,
      @Valid @RequestBody PostUpdateRequest request) {
    return ApiResponse.success(posts.update(userId, postId, request), "게시글을 수정했습니다.");
  }

  @Operation(summary = "게시글 삭제", description = "작성자 또는 담당 매니저가 게시글을 소프트 삭제합니다.")
  @DeleteMapping("/{postId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@CurrentUserId Long userId, @PathVariable Long postId) {
    posts.delete(userId, postId);
  }

  @Operation(summary = "게시글 좋아요 등록")
  @PostMapping("/{postId}/likes")
  @ResponseStatus(HttpStatus.CREATED)
  public void like(@CurrentUserId Long userId, @PathVariable Long postId) {
    postLikes.like(userId, postId);
  }

  @Operation(summary = "게시글 좋아요 취소")
  @DeleteMapping("/{postId}/likes")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unlike(@CurrentUserId Long userId, @PathVariable Long postId) {
    postLikes.unlike(userId, postId);
  }

  @Operation(summary = "게시글 좋아요 정보 조회")
  @GetMapping("/{postId}/likes")
  public ApiResponse<LikeResponse> likes(
      @CurrentUserId Long userId, @PathVariable Long postId) {
    return ApiResponse.success(postLikes.getInfo(userId, postId), "좋아요 정보입니다.");
  }
}
