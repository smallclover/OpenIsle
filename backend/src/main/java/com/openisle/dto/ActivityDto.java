package com.openisle.dto;

import com.openisle.model.ActivityType;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * DTO representing an activity without participant details.
 */
@Data
public class ActivityDto {

  private Long id;
  private String title;
  private String icon;
  private String content;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private ActivityType type;
  private boolean ended;
}
