package com.jjap.berries.post.service;

import com.jjap.berries.channel.repository.ChannelRepository;
import com.jjap.berries.community.repository.FanMembershipRepository;
import com.jjap.berries.global.exception.BusinessException;
import com.jjap.berries.global.exception.ErrorCode;
import com.jjap.berries.global.service.AccessService;
import com.jjap.berries.post.domain.Post;
import com.jjap.berries.post.domain.PostType;
import com.jjap.berries.post.dto.PostCreateRequest;
import com.jjap.berries.post.dto.PostResponse;
import com.jjap.berries.post.dto.PostUpdateRequest;
import com.jjap.berries.post.repository.PostRepository;
import com.jjap.berries.user.domain.User;
import com.jjap.berries.user.domain.UserRole;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
  private final PostRepository posts;
  private final ChannelRepository channels;
  private final FanMembershipRepository memberships;
  private final PostLikeService postLikes;
  private final AccessService access;

  public Page<PostResponse> list(Long channelId, PostType type, Pageable pageable) {
    Page<Post> page =
        type == null
            ? posts.findAllByChannelIdAndDeletedAtIsNull(channelId, pageable)
            : posts.findAllByChannelIdAndTypeAndDeletedAtIsNull(channelId, type, pageable);
    Map<Long, Long> likeCounts =
        postLikes.countsByPostIds(page.getContent().stream().map(Post::getId).toList());
    return page.map(post -> PostResponse.from(post, likeCounts.getOrDefault(post.getId(), 0L)));
  }

  public PostResponse get(Long id) {
    Post post = post(id);
    return PostResponse.from(post, postLikes.count(id));
  }

  @Transactional
  public PostResponse create(Long userId, Long channelId, PostCreateRequest request) {
    User user = access.user(userId);
    PostType type = postType(user.getRole());
    authorizeCreate(user, channelId, type);
    if (type != PostType.ARTIST && (request.title() == null || request.title().isBlank())) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }
    var channel =
        channels
            .findById(channelId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));
    Post post =
        posts.save(
            new Post(
                channel,
                user,
                request.title() == null ? "" : request.title(),
                request.content(),
                type));
    return PostResponse.from(post, 0);
  }

  @Transactional
  public PostResponse update(Long userId, Long id, PostUpdateRequest request) {
    Post post = post(id);
    if (post.getAuthor() == null || !post.getAuthor().getId().equals(userId)) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
    if (post.getType() != PostType.ARTIST
        && request.title() != null
        && request.title().isBlank()) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }
    post.update(request.title(), request.content());
    return PostResponse.from(post, postLikes.count(id));
  }

  @Transactional
  public void delete(Long userId, Long id) {
    User user = access.user(userId);
    Post post = post(id);
    if (post.getAuthor() == null || !post.getAuthor().getId().equals(userId)) {
      access.manager(user, post.getChannel().getId());
    }
    post.delete();
  }

  private void authorizeCreate(User user, Long channelId, PostType type) {
    switch (type) {
      case FAN -> {
        if (user.getRole() != UserRole.USER
            || !memberships.existsByChannelIdAndUserId(channelId, user.getId())) {
          throw new BusinessException(ErrorCode.NOT_COMMUNITY_MEMBER);
        }
      }
      case ARTIST -> {
        if (user.getRole() != UserRole.ARTIST || !access.artistMember(user, channelId)) {
          throw new BusinessException(ErrorCode.NOT_ARTIST_MEMBER);
        }
      }
      case NOTICE -> access.manager(user, channelId);
    }
  }

  private PostType postType(UserRole role) {
    return switch (role) {
      case USER -> PostType.FAN;
      case ARTIST -> PostType.ARTIST;
      case MANAGER -> PostType.NOTICE;
    };
  }

  private Post post(Long id) {
    return posts
        .findById(id)
        .filter(post -> post.getDeletedAt() == null)
        .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
  }
}
