package com.openisle.controller;

import com.openisle.dto.ConfigDto;
import com.openisle.service.AiUsageService;
import com.openisle.service.PasswordValidator;
import com.openisle.service.PostService;
import com.openisle.service.RegisterModeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
public class AdminConfigController {

  private final PostService postService;
  private final PasswordValidator passwordValidator;
  private final AiUsageService aiUsageService;
  private final RegisterModeService registerModeService;

  @GetMapping
  @SecurityRequirement(name = "JWT")
  @Operation(
    summary = "Get configuration",
    description = "Retrieve application configuration settings"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Current configuration",
    content = @Content(schema = @Schema(implementation = ConfigDto.class))
  )
  public ConfigDto getConfig() {
    ConfigDto dto = new ConfigDto();
    dto.setPublishMode(postService.getPublishMode());
    dto.setPasswordStrength(passwordValidator.getStrength());
    dto.setAiFormatLimit(aiUsageService.getFormatLimit());
    dto.setRegisterMode(registerModeService.getRegisterMode());
    return dto;
  }

  @PostMapping
  @SecurityRequirement(name = "JWT")
  @Operation(
    summary = "Update configuration",
    description = "Update application configuration settings"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Updated configuration",
    content = @Content(schema = @Schema(implementation = ConfigDto.class))
  )
  public ConfigDto updateConfig(@RequestBody ConfigDto dto) {
    if (dto.getPublishMode() != null) {
      postService.setPublishMode(dto.getPublishMode());
    }
    if (dto.getPasswordStrength() != null) {
      passwordValidator.setStrength(dto.getPasswordStrength());
    }
    if (dto.getAiFormatLimit() != null) {
      aiUsageService.setFormatLimit(dto.getAiFormatLimit());
    }
    if (dto.getRegisterMode() != null) {
      registerModeService.setRegisterMode(dto.getRegisterMode());
    }
    return getConfig();
  }
}
