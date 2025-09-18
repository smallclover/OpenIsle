package com.openisle.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class PollDto {

  private List<String> options;
  private Map<Integer, Integer> votes;
  private LocalDateTime endTime;
  private List<AuthorDto> participants;
  private Map<Integer, List<AuthorDto>> optionParticipants;
  private boolean multiple;
}
