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
@Table(name = "post_donate_change_logs")
public class PostDonateChangeLog extends PostChangeLog {

  @Column(nullable = false)
  private int amount;
}
