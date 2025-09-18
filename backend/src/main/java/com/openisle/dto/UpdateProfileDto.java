package com.openisle.dto;

import lombok.Data;

/** Request body for updating user profile. */
@Data
public class UpdateProfileDto {

  private String username;
  private String introduction;
}
