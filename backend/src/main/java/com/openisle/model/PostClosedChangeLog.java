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
@Table(name = "post_closed_change_logs")
public class PostClosedChangeLog extends PostChangeLog {

  private boolean oldClosed;
  private boolean newClosed;
}
