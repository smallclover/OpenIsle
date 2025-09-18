package com.openisle.dto;

import java.time.LocalDateTime;
import lombok.Data;

/** Lightweight post metadata used in user profile lists. */
@Data
public class PostMetaDto {

  private Long id;
  private String title;
  private String snippet;
  private LocalDateTime createdAt;
  private String category;
  private long views;
}
