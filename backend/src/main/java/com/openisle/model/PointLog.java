package com.openisle.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Daily experience gain counts for a user. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
  name = "point_logs",
  uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "log_date" })
)
public class PointLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "log_date", nullable = false)
  private LocalDate logDate;

  @Column(name = "post_count", nullable = false)
  private int postCount;

  @Column(name = "comment_count", nullable = false)
  private int commentCount;

  @Column(name = "reaction_count", nullable = false)
  private int reactionCount;
}
