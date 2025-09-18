package com.openisle.controller;

import com.openisle.dto.CommentDto;
import com.openisle.mapper.CommentMapper;
import com.openisle.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints for administrators to manage comments.
 */
@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

  private final CommentService commentService;
  private final CommentMapper commentMapper;

  @PostMapping("/{id}/pin")
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "Pin comment", description = "Pin a comment by its id")
  @ApiResponse(
    responseCode = "200",
    description = "Pinned comment",
    content = @Content(schema = @Schema(implementation = CommentDto.class))
  )
  public CommentDto pin(@PathVariable Long id, Authentication auth) {
    return commentMapper.toDto(commentService.pinComment(auth.getName(), id));
  }

  @PostMapping("/{id}/unpin")
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "Unpin comment", description = "Remove pin from a comment")
  @ApiResponse(
    responseCode = "200",
    description = "Unpinned comment",
    content = @Content(schema = @Schema(implementation = CommentDto.class))
  )
  public CommentDto unpin(@PathVariable Long id, Authentication auth) {
    return commentMapper.toDto(commentService.unpinComment(auth.getName(), id));
  }
}
