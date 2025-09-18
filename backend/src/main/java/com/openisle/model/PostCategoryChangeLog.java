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
@Table(name = "post_category_change_logs")
public class PostCategoryChangeLog extends PostChangeLog {

  private String oldCategory;
  private String newCategory;
}
