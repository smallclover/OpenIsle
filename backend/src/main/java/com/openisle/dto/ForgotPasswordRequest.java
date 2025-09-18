package com.openisle.dto;

import lombok.Data;

/** Request to trigger a forgot password email. */
@Data
public class ForgotPasswordRequest {

  private String email;
}
