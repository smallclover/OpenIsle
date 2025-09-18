package com.openisle.dto;

import com.openisle.model.NotificationType;
import com.openisle.model.ReactionType;
import java.time.LocalDateTime;
import lombok.Data;

/** DTO representing a user notification. */
@Data
public class NotificationDto {

  private Long id;
  private NotificationType type;
  private PostSummaryDto post;
  private CommentDto comment;
  private CommentDto parentComment;
  private AuthorDto fromUser;
  private ReactionType reactionType;
  private String content;
  private Boolean approved;
  private boolean read;
  private LocalDateTime createdAt;
}
