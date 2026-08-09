package com.jjap.berries.post.repository;

import com.jjap.berries.post.domain.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  List<Comment> findAllByPostIdOrderByCreatedAtAsc(Long postId);
}
