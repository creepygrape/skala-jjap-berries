package com.jjap.berries.post.service;

import com.jjap.berries.community.repository.FanMembershipRepository;
import com.jjap.berries.global.exception.BusinessException;
import com.jjap.berries.global.exception.ErrorCode;
import com.jjap.berries.global.service.AccessService;
import com.jjap.berries.post.domain.Comment;
import com.jjap.berries.post.domain.Post;
import com.jjap.berries.post.dto.CommentRequest;
import com.jjap.berries.post.dto.CommentResponse;
import com.jjap.berries.post.dto.CommentUpdateResponse;
import com.jjap.berries.post.dto.ReplyResponse;
import com.jjap.berries.post.repository.CommentRepository;
import com.jjap.berries.post.repository.PostRepository;
import com.jjap.berries.user.domain.User;
import com.jjap.berries.user.domain.UserRole;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
  private final CommentRepository comments;
  private final PostRepository posts;
  private final FanMembershipRepository memberships;
  private final AccessService access;

  public List<CommentResponse> list(Long postId) {
    post(postId);
    List<Comment> all = comments.findAllByPostIdOrderByCreatedAtAsc(postId);
    Map<Long, List<Comment>> repliesByRootId =
        all.stream()
            .filter(comment -> comment.getRootComment() != null)
            .collect(Collectors.groupingBy(comment -> comment.getRootComment().getId()));
    return all.stream()
        .filter(comment -> comment.getRootComment() == null)
        .map(
            root ->
                new CommentResponse(
                    root.getId(),
                    name(root.getAuthor()),
                    body(root),
                    root.getDeletedAt() != null,
                    root.getCreatedAt(),
                    repliesByRootId.getOrDefault(root.getId(), List.of()).stream()
                        .map(this::replyResponse)
                        .toList()))
        .toList();
  }

  @Transactional
  public CommentResponse create(Long userId, Long postId, CommentRequest request) {
    Post post = post(postId);
    User user = access.user(userId);
    authorizeComment(post, user);
    Comment comment = comments.save(Comment.root(post, user, request.content()));
    return new CommentResponse(
        comment.getId(),
        name(comment.getAuthor()),
        comment.getContent(),
        false,
        comment.getCreatedAt(),
        List.of());
  }

  @Transactional
  public ReplyResponse reply(Long userId, Long targetId, CommentRequest request) {
    Comment target = comment(targetId);
    Post post = target.getPost();
    User user = access.user(userId);
    authorizeComment(post, user);
    Comment root = target.getRootComment() == null ? target : target.getRootComment();
    Comment reply =
        comments.save(Comment.reply(post, user, request.content(), root, target));
    return replyResponse(reply);
  }

  @Transactional
  public CommentUpdateResponse update(Long userId, Long id, CommentRequest request) {
    Comment comment = comment(id);
    if (comment.getAuthor() == null || !comment.getAuthor().getId().equals(userId)) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
    comment.update(request.content());
    return CommentUpdateResponse.from(comment);
  }

  @Transactional
  public void delete(Long userId, Long id) {
    User user = access.user(userId);
    Comment comment = comment(id);
    if (comment.getAuthor() == null || !comment.getAuthor().getId().equals(userId)) {
      access.manager(user, comment.getPost().getChannel().getId());
    }
    comment.delete();
  }

  private void authorizeComment(Post post, User user) {
    Long channelId = post.getChannel().getId();
    if (user.getRole() == UserRole.USER) {
      if (!memberships.existsByChannelIdAndUserId(channelId, user.getId())) {
        throw new BusinessException(ErrorCode.NOT_COMMUNITY_MEMBER);
      }
      return;
    }
    if (user.getRole() == UserRole.ARTIST) {
      if (!access.artistMember(user, channelId)) {
        throw new BusinessException(ErrorCode.NOT_ARTIST_MEMBER);
      }
      return;
    }
    access.manager(user, channelId);
  }

  private ReplyResponse replyResponse(Comment reply) {
    return new ReplyResponse(
        reply.getId(),
        name(reply.getAuthor()),
        reply.getReplyToComment().getId(),
        name(reply.getReplyToComment().getAuthor()),
        body(reply),
        reply.getDeletedAt() != null,
        reply.getCreatedAt());
  }

  private Post post(Long id) {
    return posts
        .findById(id)
        .filter(post -> post.getDeletedAt() == null)
        .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
  }

  private Comment comment(Long id) {
    return comments
        .findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
  }

  private String name(User user) {
    return user == null ? "탈퇴한 사용자" : user.getNickname();
  }

  private String body(Comment comment) {
    return comment.getDeletedAt() == null ? comment.getContent() : "삭제된 댓글입니다.";
  }
}
