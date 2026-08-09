package com.jjap.berries.post.repository;

import com.jjap.berries.post.domain.PostLike;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
  boolean existsByPostIdAndUserId(Long postId, Long userId);

  Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

  long countByPostId(Long postId);

  @Query(
      """
      select postLike.post.id as postId, count(postLike.id) as likeCount
      from PostLike postLike
      where postLike.post.id in :postIds
      group by postLike.post.id
      """)
  List<PostLikeCount> countAllByPostIdIn(@Param("postIds") List<Long> postIds);
}
