package com.openisle.dto;

import java.time.LocalDateTime;
import lombok.Data;

/** DTO for a user's post read record. */
@Data
public class PostReadDto {

  private PostMetaDto post;
  private LocalDateTime lastReadAt;
}
