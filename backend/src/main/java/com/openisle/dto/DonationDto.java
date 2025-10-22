package com.openisle.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonationDto {

  private Long userId;
  private String username;
  private String avatar;
  private int amount;
  private LocalDateTime createdAt;
}
