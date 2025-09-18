package com.openisle.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "post_pinned_change_logs")
public class PostPinnedChangeLog extends PostChangeLog {

  private LocalDateTime oldPinnedAt;
  private LocalDateTime newPinnedAt;
}
