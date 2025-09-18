package com.openisle.repository;

import com.openisle.model.Comment;
import com.openisle.model.CommentSubscription;
import com.openisle.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentSubscriptionRepository extends JpaRepository<CommentSubscription, Long> {
  List<CommentSubscription> findByComment(Comment comment);
  List<CommentSubscription> findByUser(User user);
  Optional<CommentSubscription> findByUserAndComment(User user, Comment comment);
}
