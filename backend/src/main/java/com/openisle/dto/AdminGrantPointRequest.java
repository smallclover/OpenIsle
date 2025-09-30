package com.openisle.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminGrantPointRequest {

  private String username;
  private int amount;
}
