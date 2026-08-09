package com.jjap.berries.post.service;

import com.jjap.berries.global.exception.BusinessException;
import com.jjap.berries.global.exception.ErrorCode;
import com.jjap.berries.global.service.AccessService;
import com.jjap.berries.post.domain.Post;
import com.jjap.berries.post.domain.PostLike;
import com.jjap.berries.post.dto.LikeResponse;
import com.jjap.berries.post.repository.PostLikeCount;
import com.jjap.berries.post.repository.PostLikeRepository;
import com.jjap.berries.post.repository.PostRepository;
import com.jjap.berries.user.domain.User;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {
  private final PostLikeRepository likes;
  private final PostRepository posts;
  private final AccessService access;

  @Transactional
  public void like(Long userId, Long postId) {
    Post post = post(postId);
    User user = access.user(userId);
    if (likes.existsByPostIdAndUserId(postId, userId)) {
      throw new BusinessException(ErrorCode.LIKE_ALREADY_EXISTS);
    }
    likes.save(new PostLike(post, user));
  }

  @Transactional
  public void unlike(Long userId, Long postId) {
    post(postId);
    likes
        .findByPostIdAndUserId(postId, userId)
        .ifPresentOrElse(
            likes::delete,
            () -> {
              throw new BusinessException(ErrorCode.LIKE_NOT_FOUND);
            });
  }

  public LikeResponse getInfo(Long userId, Long postId) {
    post(postId);
    return new LikeResponse(
        count(postId), userId != null && likes.existsByPostIdAndUserId(postId, userId));
  }

  public long count(Long postId) {
    return likes.countByPostId(postId);
  }

  public Map<Long, Long> countsByPostIds(List<Long> postIds) {
    if (postIds.isEmpty()) {
      return Map.of();
    }
    return likes.countAllByPostIdIn(postIds).stream()
        .collect(Collectors.toMap(PostLikeCount::getPostId, PostLikeCount::getLikeCount));
  }

  private Post post(Long id) {
    return posts
        .findById(id)
        .filter(post -> post.getDeletedAt() == null)
        .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
  }
}
