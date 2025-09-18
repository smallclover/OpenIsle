package com.openisle.dto;

import com.openisle.model.RegisterMode;
import lombok.Data;

/** Public site configuration values. */
@Data
public class SiteConfigDto {

  private boolean captchaEnabled;
  private boolean registerCaptchaEnabled;
  private boolean loginCaptchaEnabled;
  private boolean postCaptchaEnabled;
  private boolean commentCaptchaEnabled;
  private int aiFormatLimit;
  private RegisterMode registerMode;
}
