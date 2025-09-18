package com.openisle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

  @GetMapping("/api/hello")
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "Hello endpoint", description = "Returns a greeting for authenticated users")
  @ApiResponse(
    responseCode = "200",
    description = "Greeting payload",
    content = @Content(schema = @Schema(implementation = Map.class))
  )
  public Map<String, String> hello() {
    return Map.of("message", "Hello, Authenticated User");
  }
}
