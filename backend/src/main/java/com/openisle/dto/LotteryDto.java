package com.openisle.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** Metadata for lottery posts. */
@Data
public class LotteryDto {

  private String prizeDescription;
  private String prizeIcon;
  private int prizeCount;
  private int pointCost;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private List<AuthorDto> participants;
  private List<AuthorDto> winners;
}
