package com.openisle.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConversationDto {

  private Long id;
  private String name;
  private boolean channel;
  private String avatar;
  private MessageDto lastMessage;
  private List<UserSummaryDto> participants;
  private LocalDateTime createdAt;
  private long unreadCount;
}
