package com.openisle.controller;

import com.openisle.dto.PostSummaryDto;
import com.openisle.mapper.PostMapper;
import com.openisle.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints for administrators to manage posts.
 */
@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {

  private final PostService postService;
  private final PostMapper postMapper;

  @GetMapping("/pending")
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "List pending posts", description = "Retrieve posts awaiting approval")
  @ApiResponse(
    responseCode = "200",
    description = "Pending posts",
    content = @Content(
      array = @ArraySchema(schema = @Schema(implementation = PostSummaryDto.class))
    )
  )
  public List<PostSummaryDto> pendingPosts() {
    return postService
      .listPendingPosts()
      .stream()
      .map(postMapper::toSummaryDto)
      .collect(Collectors.toList());
  }

  @PostMapping("/{id}/approve")
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "Approve post", description = "Approve a pending post")
  @ApiResponse(
    responseCode = "200",
    description = "Approved post",
    content = @Content(schema = @Schema(implementation = PostSummaryDto.class))
  )
  public PostSummaryDto approve(@PathVariable Long id) {
    return postMapper.toSummaryDto(postService.approvePost(id));
  }

  @PostMapping("/{id}/reject")
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "Reject post", description = "Reject a pending post")
  @ApiResponse(
    responseCode = "200",
    description = "Rejected post",
    content = @Content(schema = @Schema(implementation = PostSummaryDto.class))
  )
  public PostSummaryDto reject(@PathVariable Long id) {
    return postMapper.toSummaryDto(postService.rejectPost(id));
  }

  @PostMapping("/{id}/pin")
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "Pin post", description = "Pin a post to the top")
  @ApiResponse(
    responseCode = "200",
    description = "Pinned post",
    content = @Content(schema = @Schema(implementation = PostSummaryDto.class))
  )
  public PostSummaryDto pin(
    @PathVariable Long id,
    org.springframework.security.core.Authentication auth
  ) {
    return postMapper.toSummaryDto(postService.pinPost(id, auth.getName()));
  }

  @PostMapping("/{id}/unpin")
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "Unpin post", description = "Remove a post from the top")
  @ApiResponse(
    responseCode = "200",
    description = "Unpinned post",
    content = @Content(schema = @Schema(implementation = PostSummaryDto.class))
  )
  public PostSummaryDto unpin(
    @PathVariable Long id,
    org.springframework.security.core.Authentication auth
  ) {
    return postMapper.toSummaryDto(postService.unpinPost(id, auth.getName()));
  }

  @PostMapping("/{id}/rss-exclude")
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "Exclude from RSS", description = "Exclude a post from RSS feed")
  @ApiResponse(
    responseCode = "200",
    description = "Updated post",
    content = @Content(schema = @Schema(implementation = PostSummaryDto.class))
  )
  public PostSummaryDto excludeFromRss(
    @PathVariable Long id,
    org.springframework.security.core.Authentication auth
  ) {
    return postMapper.toSummaryDto(postService.excludeFromRss(id, auth.getName()));
  }

  @PostMapping("/{id}/rss-include")
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "Include in RSS", description = "Include a post in the RSS feed")
  @ApiResponse(
    responseCode = "200",
    description = "Updated post",
    content = @Content(schema = @Schema(implementation = PostSummaryDto.class))
  )
  public PostSummaryDto includeInRss(
    @PathVariable Long id,
    org.springframework.security.core.Authentication auth
  ) {
    return postMapper.toSummaryDto(postService.includeInRss(id, auth.getName()));
  }
}
