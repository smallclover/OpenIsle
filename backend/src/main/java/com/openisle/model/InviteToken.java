package com.openisle.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Data;

/**
 * Invite token entity tracking usage counts.
 */
@Data
@Entity
public class InviteToken {

  @Id
  private String token;

  /**
   * Short token used in invite links. Existing records may have this field null
   * and fall back to {@link #token} for backward compatibility.
   */
  @Column(unique = true)
  private String shortToken;

  @ManyToOne
  private User inviter;

  private LocalDate createdDate;

  private int usageCount;
}
