package com.openisle.dto;

import java.time.LocalDateTime;
import lombok.*;

/**
 * comment and change_log Dto
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimelineItemDto<T> {

  private Long id;
  private String kind; // "comment" | "log"
  private LocalDateTime createdAt;
  private LocalDateTime pinnedAt;
  private T payload; // 泛型，具体类型由外部决定
}
