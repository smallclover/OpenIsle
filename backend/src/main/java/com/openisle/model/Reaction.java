package com.openisle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Reaction entity representing a user's reaction to a post, comment or message.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
  name = "reactions",
  uniqueConstraints = {
    @UniqueConstraint(columnNames = { "user_id", "post_id", "type" }),
    @UniqueConstraint(columnNames = { "user_id", "comment_id", "type" }),
    @UniqueConstraint(columnNames = { "user_id", "message_id", "type" }),
  }
)
public class Reaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReactionType type;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "comment_id")
  private Comment comment;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "message_id")
  private Message message;

  @CreationTimestamp
  @Column(
    nullable = false,
    updatable = false,
    columnDefinition = "DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)"
  )
  private java.time.LocalDateTime createdAt;
}
