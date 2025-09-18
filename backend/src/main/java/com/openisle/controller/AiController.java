package com.openisle.controller;

import com.openisle.service.AiUsageService;
import com.openisle.service.OpenAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

  private final OpenAiService openAiService;
  private final AiUsageService aiUsageService;

  @PostMapping("/format")
  @Operation(summary = "Format markdown", description = "Format text via AI")
  @ApiResponse(
    responseCode = "200",
    description = "Formatted content",
    content = @Content(schema = @Schema(implementation = Map.class))
  )
  @SecurityRequirement(name = "JWT")
  public ResponseEntity<Map<String, String>> format(
    @RequestBody Map<String, String> req,
    Authentication auth
  ) {
    String text = req.get("text");
    if (text == null) {
      return ResponseEntity.badRequest().build();
    }
    int limit = aiUsageService.getFormatLimit();
    int used = aiUsageService.getCount(auth.getName());
    if (limit > 0 && used >= limit) {
      return ResponseEntity.status(429).build();
    }
    aiUsageService.incrementAndGetCount(auth.getName());
    return openAiService
      .formatMarkdown(text)
      .map(t -> ResponseEntity.ok(Map.of("content", t)))
      .orElse(ResponseEntity.status(500).build());
  }
}
