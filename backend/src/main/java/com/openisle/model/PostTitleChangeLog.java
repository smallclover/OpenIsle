package com.openisle.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "post_title_change_logs")
public class PostTitleChangeLog extends PostChangeLog {

  private String oldTitle;
  private String newTitle;
}
