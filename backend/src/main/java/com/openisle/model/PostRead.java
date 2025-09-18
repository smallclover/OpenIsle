package com.openisle.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Record of a user reading a post. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
  name = "post_reads",
  uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "post_id" })
)
public class PostRead {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "post_id")
  private Post post;

  @Column(name = "last_read_at")
  private LocalDateTime lastReadAt;
}
