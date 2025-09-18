package com.openisle.dto;

import java.util.List;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
public class ConversationDetailDto {

  private Long id;
  private String name;
  private boolean channel;
  private String avatar;
  private List<UserSummaryDto> participants;
  private Page<MessageDto> messages;
}
