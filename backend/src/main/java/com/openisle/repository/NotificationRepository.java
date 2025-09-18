package com.openisle.repository;

import com.openisle.model.Comment;
import com.openisle.model.Notification;
import com.openisle.model.NotificationType;
import com.openisle.model.Post;
import com.openisle.model.ReactionType;
import com.openisle.model.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for Notification entities. */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findByUserOrderByCreatedAtDesc(User user);
  List<Notification> findByUserAndReadOrderByCreatedAtDesc(User user, boolean read);
  Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
  Page<Notification> findByUserAndReadOrderByCreatedAtDesc(
    User user,
    boolean read,
    Pageable pageable
  );
  Page<Notification> findByUserAndTypeNotInOrderByCreatedAtDesc(
    User user,
    java.util.Collection<NotificationType> types,
    Pageable pageable
  );
  Page<Notification> findByUserAndReadAndTypeNotInOrderByCreatedAtDesc(
    User user,
    boolean read,
    java.util.Collection<NotificationType> types,
    Pageable pageable
  );
  long countByUserAndRead(User user, boolean read);
  long countByUserAndReadAndTypeNotIn(
    User user,
    boolean read,
    java.util.Collection<NotificationType> types
  );
  List<Notification> findByPost(Post post);
  List<Notification> findByComment(Comment comment);

  void deleteByTypeAndFromUser(NotificationType type, User fromUser);

  List<Notification> findByTypeAndFromUser(NotificationType type, User fromUser);

  void deleteByTypeAndFromUserAndPost(NotificationType type, User fromUser, Post post);

  void deleteByTypeAndFromUserAndPostAndReactionType(
    NotificationType type,
    User fromUser,
    Post post,
    ReactionType reactionType
  );

  void deleteByTypeAndFromUserAndCommentAndReactionType(
    NotificationType type,
    User fromUser,
    Comment comment,
    ReactionType reactionType
  );
}
