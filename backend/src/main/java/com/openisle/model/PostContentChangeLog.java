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
@Table(name = "post_content_change_logs")
public class PostContentChangeLog extends PostChangeLog {

  @Column(name = "old_content", columnDefinition = "LONGTEXT")
  private String oldContent;

  @Column(name = "new_content", columnDefinition = "LONGTEXT")
  private String newContent;
}
