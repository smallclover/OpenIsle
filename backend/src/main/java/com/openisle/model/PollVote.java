package com.openisle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
  name = "poll_votes",
  uniqueConstraints = @UniqueConstraint(columnNames = { "post_id", "user_id", "option_index" })
)
@Getter
@Setter
@NoArgsConstructor
public class PollVote {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "post_id")
  private PollPost post;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "option_index", nullable = false)
  private int optionIndex;
}
