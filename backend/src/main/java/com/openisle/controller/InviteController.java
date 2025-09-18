package com.openisle.controller;

import com.openisle.service.InviteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invite")
@RequiredArgsConstructor
public class InviteController {

  private final InviteService inviteService;

  @PostMapping("/generate")
  @Operation(summary = "Generate invite", description = "Generate an invite token")
  @ApiResponse(
    responseCode = "200",
    description = "Invite token",
    content = @Content(schema = @Schema(implementation = Map.class))
  )
  @SecurityRequirement(name = "JWT")
  public Map<String, String> generate(Authentication auth) {
    String token = inviteService.generate(auth.getName());
    return Map.of("token", token);
  }
}
