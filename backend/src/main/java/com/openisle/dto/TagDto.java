package com.openisle.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * DTO representing a tag.
 */
@Data
public class TagDto {

  private Long id;
  private String name;
  private String description;
  private String icon;
  private String smallIcon;
  private LocalDateTime createdAt;
  private Long count;
}
