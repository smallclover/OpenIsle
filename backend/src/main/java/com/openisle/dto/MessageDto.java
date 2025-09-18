package com.openisle.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class MessageDto {

  private Long id;
  private String content;
  private UserSummaryDto sender;
  private Long conversationId;
  private LocalDateTime createdAt;
  private MessageDto replyTo;
  private List<ReactionDto> reactions;
}
