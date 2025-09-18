package com.openisle.controller;

import com.openisle.dto.DraftDto;
import com.openisle.dto.DraftRequest;
import com.openisle.mapper.DraftMapper;
import com.openisle.model.Draft;
import com.openisle.service.DraftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drafts")
@RequiredArgsConstructor
public class DraftController {

  private final DraftService draftService;
  private final DraftMapper draftMapper;

  @PostMapping
  @Operation(summary = "Save draft", description = "Save a draft for current user")
  @ApiResponse(
    responseCode = "200",
    description = "Draft saved",
    content = @Content(schema = @Schema(implementation = DraftDto.class))
  )
  @SecurityRequirement(name = "JWT")
  public ResponseEntity<DraftDto> saveDraft(@RequestBody DraftRequest req, Authentication auth) {
    Draft draft = draftService.saveDraft(
      auth.getName(),
      req.getCategoryId(),
      req.getTitle(),
      req.getContent(),
      req.getTagIds()
    );
    return ResponseEntity.ok(draftMapper.toDto(draft));
  }

  @GetMapping("/me")
  @Operation(summary = "Get my draft", description = "Get current user's draft")
  @ApiResponse(
    responseCode = "200",
    description = "Draft details",
    content = @Content(schema = @Schema(implementation = DraftDto.class))
  )
  @SecurityRequirement(name = "JWT")
  public ResponseEntity<DraftDto> getMyDraft(Authentication auth) {
    return draftService
      .getDraft(auth.getName())
      .map(d -> ResponseEntity.ok(draftMapper.toDto(d)))
      .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @DeleteMapping("/me")
  @Operation(summary = "Delete my draft", description = "Delete current user's draft")
  @ApiResponse(responseCode = "200", description = "Draft deleted")
  @SecurityRequirement(name = "JWT")
  public ResponseEntity<?> deleteMyDraft(Authentication auth) {
    draftService.deleteDraft(auth.getName());
    return ResponseEntity.ok().build();
  }
}
