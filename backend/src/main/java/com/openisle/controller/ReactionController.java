package com.openisle.controller;

import com.openisle.dto.ReactionDto;
import com.openisle.dto.ReactionRequest;
import com.openisle.mapper.ReactionMapper;
import com.openisle.model.Reaction;
import com.openisle.model.ReactionType;
import com.openisle.service.LevelService;
import com.openisle.service.PointService;
import com.openisle.service.ReactionService;
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
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReactionController {

  private final ReactionService reactionService;
  private final LevelService levelService;
  private final ReactionMapper reactionMapper;
  private final PointService pointService;

  /**
   * Get all available reaction types.
   */
  @GetMapping("/reaction-types")
  @Operation(summary = "List reaction types", description = "Get all available reaction types")
  @ApiResponse(
    responseCode = "200",
    description = "Reaction types",
    content = @Content(schema = @Schema(implementation = ReactionType[].class))
  )
  public ReactionType[] listReactionTypes() {
    return ReactionType.values();
  }

  @PostMapping("/posts/{postId}/reactions")
  @Operation(summary = "React to post", description = "React to a post")
  @ApiResponse(
    responseCode = "200",
    description = "Reaction result",
    content = @Content(schema = @Schema(implementation = ReactionDto.class))
  )
  @SecurityRequirement(name = "JWT")
  public ResponseEntity<ReactionDto> reactToPost(
    @PathVariable Long postId,
    @RequestBody ReactionRequest req,
    Authentication auth
  ) {
    Reaction reaction = reactionService.reactToPost(auth.getName(), postId, req.getType());
    if (reaction == null) {
      pointService.deductForReactionOfPost(auth.getName(), postId);
      return ResponseEntity.noContent().build();
    }
    ReactionDto dto = reactionMapper.toDto(reaction);
    dto.setReward(levelService.awardForReaction(auth.getName()));
    pointService.awardForReactionOfPost(auth.getName(), postId);
    return ResponseEntity.ok(dto);
  }

  @PostMapping("/comments/{commentId}/reactions")
  @Operation(summary = "React to comment", description = "React to a comment")
  @ApiResponse(
    responseCode = "200",
    description = "Reaction result",
    content = @Content(schema = @Schema(implementation = ReactionDto.class))
  )
  @SecurityRequirement(name = "JWT")
  public ResponseEntity<ReactionDto> reactToComment(
    @PathVariable Long commentId,
    @RequestBody ReactionRequest req,
    Authentication auth
  ) {
    Reaction reaction = reactionService.reactToComment(auth.getName(), commentId, req.getType());
    if (reaction == null) {
      pointService.deductForReactionOfComment(auth.getName(), commentId);
      return ResponseEntity.noContent().build();
    }
    ReactionDto dto = reactionMapper.toDto(reaction);
    dto.setReward(levelService.awardForReaction(auth.getName()));
    pointService.awardForReactionOfComment(auth.getName(), commentId);
    return ResponseEntity.ok(dto);
  }

  @PostMapping("/messages/{messageId}/reactions")
  @Operation(summary = "React to message", description = "React to a message")
  @ApiResponse(
    responseCode = "200",
    description = "Reaction result",
    content = @Content(schema = @Schema(implementation = ReactionDto.class))
  )
  @SecurityRequirement(name = "JWT")
  public ResponseEntity<ReactionDto> reactToMessage(
    @PathVariable Long messageId,
    @RequestBody ReactionRequest req,
    Authentication auth
  ) {
    Reaction reaction = reactionService.reactToMessage(auth.getName(), messageId, req.getType());
    if (reaction == null) {
      return ResponseEntity.noContent().build();
    }
    ReactionDto dto = reactionMapper.toDto(reaction);
    dto.setReward(levelService.awardForReaction(auth.getName()));
    return ResponseEntity.ok(dto);
  }
}
