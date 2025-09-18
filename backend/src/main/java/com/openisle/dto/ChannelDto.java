package com.openisle.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChannelDto {

  private Long id;
  private String name;
  private String description;
  private String avatar;
  private MessageDto lastMessage;
  private long memberCount;
  private boolean joined;
  private long unreadCount;
}
