package com.openisle.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "poll_posts")
@Getter
@Setter
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "post_id")
public class PollPost extends Post {

  @ElementCollection
  @CollectionTable(name = "poll_post_options", joinColumns = @JoinColumn(name = "post_id"))
  @Column(name = "option_text")
  private List<String> options = new ArrayList<>();

  @ElementCollection
  @CollectionTable(name = "poll_post_votes", joinColumns = @JoinColumn(name = "post_id"))
  @MapKeyColumn(name = "option_index")
  @Column(name = "vote_count")
  private Map<Integer, Integer> votes = new HashMap<>();

  @ManyToMany
  @JoinTable(
    name = "poll_participants",
    joinColumns = @JoinColumn(name = "post_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id")
  )
  private Set<User> participants = new HashSet<>();

  @Column
  private Boolean multiple = false;

  @Column
  private LocalDateTime endTime;

  @Column
  private boolean resultAnnounced = false;
}
