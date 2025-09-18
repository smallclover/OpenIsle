package com.openisle.repository;

import com.openisle.model.Post;
import com.openisle.model.PostRead;
import com.openisle.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostReadRepository extends JpaRepository<PostRead, Long> {
  Optional<PostRead> findByUserAndPost(User user, Post post);
  long countByUser(User user);
  void deleteByPost(Post post);
}
