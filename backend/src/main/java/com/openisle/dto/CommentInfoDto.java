package com.openisle.dto;

import java.time.LocalDateTime;
import lombok.Data;

/** DTO for comment information in user profiles. */
@Data
public class CommentInfoDto {

  private Long id;
  private String content;
  private LocalDateTime createdAt;
  private PostMetaDto post;
  private ParentCommentDto parentComment;
}
