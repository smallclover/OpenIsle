package com.openisle.dto;

import lombok.Data;

/** Request to login. */
@Data
public class LoginRequest {

  private String username;
  private String password;
  private String captcha;
}
