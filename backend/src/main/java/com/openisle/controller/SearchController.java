package com.openisle.controller;

import com.openisle.dto.PostSummaryDto;
import com.openisle.dto.SearchResultDto;
import com.openisle.dto.UserDto;
import com.openisle.mapper.PostMapper;
import com.openisle.mapper.UserMapper;
import com.openisle.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

  private final SearchService searchService;
  private final UserMapper userMapper;
  private final PostMapper postMapper;

  @GetMapping("/users")
  @Operation(summary = "Search users", description = "Search users by keyword")
  @ApiResponse(
    responseCode = "200",
    description = "List of users",
    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))
  )
  public List<UserDto> searchUsers(@RequestParam String keyword) {
    return searchService
      .searchUsers(keyword)
      .stream()
      .map(userMapper::toDto)
      .collect(Collectors.toList());
  }

  @GetMapping("/posts")
  @Operation(summary = "Search posts", description = "Search posts by keyword")
  @ApiResponse(
    responseCode = "200",
    description = "List of posts",
    content = @Content(
      array = @ArraySchema(schema = @Schema(implementation = PostSummaryDto.class))
    )
  )
  public List<PostSummaryDto> searchPosts(@RequestParam String keyword) {
    return searchService
      .searchPosts(keyword)
      .stream()
      .map(postMapper::toSummaryDto)
      .collect(Collectors.toList());
  }

  @GetMapping("/posts/content")
  @Operation(summary = "Search posts by content", description = "Search posts by content keyword")
  @ApiResponse(
    responseCode = "200",
    description = "List of posts",
    content = @Content(
      array = @ArraySchema(schema = @Schema(implementation = PostSummaryDto.class))
    )
  )
  public List<PostSummaryDto> searchPostsByContent(@RequestParam String keyword) {
    return searchService
      .searchPostsByContent(keyword)
      .stream()
      .map(postMapper::toSummaryDto)
      .collect(Collectors.toList());
  }

  @GetMapping("/posts/title")
  @Operation(summary = "Search posts by title", description = "Search posts by title keyword")
  @ApiResponse(
    responseCode = "200",
    description = "List of posts",
    content = @Content(
      array = @ArraySchema(schema = @Schema(implementation = PostSummaryDto.class))
    )
  )
  public List<PostSummaryDto> searchPostsByTitle(@RequestParam String keyword) {
    return searchService
      .searchPostsByTitle(keyword)
      .stream()
      .map(postMapper::toSummaryDto)
      .collect(Collectors.toList());
  }

  @GetMapping("/global")
  @Operation(summary = "Global search", description = "Search users and posts globally")
  @ApiResponse(
    responseCode = "200",
    description = "Search results",
    content = @Content(
      array = @ArraySchema(schema = @Schema(implementation = SearchResultDto.class))
    )
  )
  public List<SearchResultDto> global(@RequestParam String keyword) {
    return searchService
      .globalSearch(keyword)
      .stream()
      .map(r -> {
        SearchResultDto dto = new SearchResultDto();
        dto.setType(r.type());
        dto.setId(r.id());
        dto.setText(r.text());
        dto.setSubText(r.subText());
        dto.setExtra(r.extra());
        dto.setPostId(r.postId());
        return dto;
      })
      .collect(Collectors.toList());
  }
}
