package com.openisle.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "post_tag_change_logs")
public class PostTagChangeLog extends PostChangeLog {

  @Column(name = "old_tags")
  private String oldTags;

  @Column(name = "new_tags")
  private String newTags;
}
