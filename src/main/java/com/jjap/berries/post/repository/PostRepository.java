package com.jjap.berries.post.repository;

import com.jjap.berries.post.domain.Post;
import com.jjap.berries.post.domain.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
  Page<Post> findAllByChannelIdAndDeletedAtIsNull(Long channelId, Pageable pageable);

  Page<Post> findAllByChannelIdAndTypeAndDeletedAtIsNull(
      Long channelId, PostType type, Pageable pageable);
}
