package com.openisle.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "post_visible_scope_change_logs")
public class PostVisibleScopeChangeLog extends PostChangeLog {

  @Enumerated(EnumType.STRING)
  private PostVisibleScopeType oldVisibleScope;

  @Enumerated(EnumType.STRING)
  private PostVisibleScopeType newVisibleScope;
}
