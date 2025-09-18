package com.openisle.dto;

import lombok.Data;

/** Request to verify a forgot password code. */
@Data
public class VerifyForgotRequest {

  private String email;
  private String code;
}
