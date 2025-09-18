package com.openisle.controller;

import com.openisle.dto.PushPublicKeyDto;
import com.openisle.dto.PushSubscriptionRequest;
import com.openisle.service.PushSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushSubscriptionController {

  private final PushSubscriptionService pushSubscriptionService;

  @Value("${app.webpush.public-key}")
  private String publicKey;

  @GetMapping("/public-key")
  @Operation(summary = "Get public key", description = "Retrieve web push public key")
  @ApiResponse(
    responseCode = "200",
    description = "Public key",
    content = @Content(schema = @Schema(implementation = PushPublicKeyDto.class))
  )
  public PushPublicKeyDto getPublicKey() {
    PushPublicKeyDto r = new PushPublicKeyDto();
    r.setKey(publicKey);
    return r;
  }

  @PostMapping("/subscribe")
  @Operation(summary = "Subscribe", description = "Subscribe to push notifications")
  @ApiResponse(responseCode = "200", description = "Subscribed")
  @SecurityRequirement(name = "JWT")
  public void subscribe(@RequestBody PushSubscriptionRequest req, Authentication auth) {
    pushSubscriptionService.saveSubscription(
      auth.getName(),
      req.getEndpoint(),
      req.getP256dh(),
      req.getAuth()
    );
  }
}
