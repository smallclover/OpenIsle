package com.openisle.dto;

import com.openisle.model.ReactionType;
import lombok.Data;

/**
 * DTO representing a reaction on a post, comment or message.
 */
@Data
public class ReactionDto {

  private Long id;
  private ReactionType type;
  private String user;
  private Long postId;
  private Long commentId;
  private Long messageId;
  private int reward;
}
