package com.openisle.dto;

import lombok.Data;

/** Request to reset password. */
@Data
public class ResetPasswordRequest {

  private String token;
  private String password;
}
