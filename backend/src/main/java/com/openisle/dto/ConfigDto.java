package com.openisle.dto;

import com.openisle.model.PasswordStrength;
import com.openisle.model.PublishMode;
import com.openisle.model.RegisterMode;
import lombok.Data;

/** DTO for site configuration. */
@Data
public class ConfigDto {

  private PublishMode publishMode;
  private PasswordStrength passwordStrength;
  private Integer aiFormatLimit;
  private RegisterMode registerMode;
}
