package com.openisle.controller;

import com.openisle.dto.MedalDto;
import com.openisle.dto.MedalSelectRequest;
import com.openisle.service.MedalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medals")
@RequiredArgsConstructor
public class MedalController {

  private final MedalService medalService;

  @GetMapping
  @Operation(summary = "List medals", description = "List medals for user or globally")
  @ApiResponse(
    responseCode = "200",
    description = "List of medals",
    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MedalDto.class)))
  )
  public List<MedalDto> getMedals(@RequestParam(value = "userId", required = false) Long userId) {
    return medalService.getMedals(userId);
  }

  @PostMapping("/select")
  @Operation(summary = "Select medal", description = "Select a medal for current user")
  @ApiResponse(responseCode = "200", description = "Medal selected")
  @SecurityRequirement(name = "JWT")
  public ResponseEntity<Void> selectMedal(
    @RequestBody MedalSelectRequest req,
    Authentication auth
  ) {
    try {
      medalService.selectMedal(auth.getName(), req.getType());
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }
}
