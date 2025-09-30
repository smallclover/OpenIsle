package com.openisle.controller;

import com.openisle.dto.AdminGrantPointRequest;
import com.openisle.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/points")
@RequiredArgsConstructor
public class AdminPointController {

  private final PointService pointService;

  @PostMapping("/grant")
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "Grant points", description = "Grant points to a user as administrator")
  @ApiResponse(responseCode = "200", description = "Points granted")
  public Map<String, Object> grant(
    @RequestBody AdminGrantPointRequest request,
    Authentication auth
  ) {
    String username = request.getUsername();
    int balance = pointService.grantPointByAdmin(auth.getName(), username, request.getAmount());
    return Map.of("username", username.trim(), "point", balance);
  }
}
