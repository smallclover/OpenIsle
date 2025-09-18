package com.openisle.controller;

import com.openisle.dto.SiteConfigDto;
import com.openisle.service.RegisterModeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@lombok.RequiredArgsConstructor
public class ConfigController {

  @Value("${app.captcha.enabled:false}")
  private boolean captchaEnabled;

  @Value("${app.captcha.register-enabled:false}")
  private boolean registerCaptchaEnabled;

  @Value("${app.captcha.login-enabled:false}")
  private boolean loginCaptchaEnabled;

  @Value("${app.captcha.post-enabled:false}")
  private boolean postCaptchaEnabled;

  @Value("${app.captcha.comment-enabled:false}")
  private boolean commentCaptchaEnabled;

  @Value("${app.ai.format-limit:3}")
  private int aiFormatLimit;

  private final RegisterModeService registerModeService;

  @GetMapping("/config")
  @Operation(summary = "Site config", description = "Get site configuration")
  @ApiResponse(
    responseCode = "200",
    description = "Site configuration",
    content = @Content(schema = @Schema(implementation = SiteConfigDto.class))
  )
  public SiteConfigDto getConfig() {
    SiteConfigDto resp = new SiteConfigDto();
    resp.setCaptchaEnabled(captchaEnabled);
    resp.setRegisterCaptchaEnabled(registerCaptchaEnabled);
    resp.setLoginCaptchaEnabled(loginCaptchaEnabled);
    resp.setPostCaptchaEnabled(postCaptchaEnabled);
    resp.setCommentCaptchaEnabled(commentCaptchaEnabled);
    resp.setAiFormatLimit(aiFormatLimit);
    resp.setRegisterMode(registerModeService.getRegisterMode());
    return resp;
  }
}
