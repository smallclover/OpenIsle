package com.openisle.controller;

import com.openisle.dto.TagDto;
import com.openisle.mapper.TagMapper;
import com.openisle.model.Tag;
import com.openisle.service.PostService;
import com.openisle.service.TagService;
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

@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
public class AdminTagController {

  private final TagService tagService;
  private final PostService postService;
  private final TagMapper tagMapper;

  @GetMapping("/pending")
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "List pending tags", description = "Retrieve tags awaiting approval")
  @ApiResponse(
    responseCode = "200",
    description = "Pending tags",
    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TagDto.class)))
  )
  public List<TagDto> pendingTags() {
    return tagService
      .listPendingTags()
      .stream()
      .map(t -> tagMapper.toDto(t, postService.countPostsByTag(t.getId())))
      .collect(Collectors.toList());
  }

  @PostMapping("/{id}/approve")
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "Approve tag", description = "Approve a pending tag")
  @ApiResponse(
    responseCode = "200",
    description = "Approved tag",
    content = @Content(schema = @Schema(implementation = TagDto.class))
  )
  public TagDto approve(@PathVariable Long id) {
    Tag tag = tagService.approveTag(id);
    long count = postService.countPostsByTag(tag.getId());
    return tagMapper.toDto(tag, count);
  }
}
