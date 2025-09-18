package com.openisle.controller;

import com.openisle.service.StatService;
import com.openisle.service.UserVisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatController {

  private final UserVisitService userVisitService;
  private final StatService statService;

  @GetMapping("/dau")
  @Operation(summary = "Daily active users", description = "Get daily active user count")
  @ApiResponse(
    responseCode = "200",
    description = "DAU count",
    content = @Content(schema = @Schema(implementation = java.util.Map.class))
  )
  public Map<String, Long> dau(
    @RequestParam(value = "date", required = false) @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE
    ) LocalDate date
  ) {
    long count = userVisitService.countDau(date);
    return Map.of("dau", count);
  }

  @GetMapping("/dau-range")
  @Operation(summary = "DAU range", description = "Get daily active users over range of days")
  @ApiResponse(
    responseCode = "200",
    description = "DAU data",
    content = @Content(array = @ArraySchema(schema = @Schema(implementation = java.util.Map.class)))
  )
  public List<Map<String, Object>> dauRange(
    @RequestParam(value = "days", defaultValue = "30") int days
  ) {
    if (days < 1) days = 1;
    LocalDate end = LocalDate.now();
    LocalDate start = end.minusDays(days - 1L);
    var data = userVisitService.countDauRange(start, end);
    return data
      .entrySet()
      .stream()
      .map(e -> Map.<String, Object>of("date", e.getKey().toString(), "value", e.getValue()))
      .toList();
  }

  @GetMapping("/new-users-range")
  @Operation(summary = "New users range", description = "Get new users over range of days")
  @ApiResponse(
    responseCode = "200",
    description = "New user data",
    content = @Content(array = @ArraySchema(schema = @Schema(implementation = java.util.Map.class)))
  )
  public List<Map<String, Object>> newUsersRange(
    @RequestParam(value = "days", defaultValue = "30") int days
  ) {
    if (days < 1) days = 1;
    LocalDate end = LocalDate.now();
    LocalDate start = end.minusDays(days - 1L);
    var data = statService.countNewUsersRange(start, end);
    return data
      .entrySet()
      .stream()
      .map(e -> Map.<String, Object>of("date", e.getKey().toString(), "value", e.getValue()))
      .toList();
  }

  @GetMapping("/posts-range")
  @Operation(summary = "Posts range", description = "Get posts count over range of days")
  @ApiResponse(
    responseCode = "200",
    description = "Post data",
    content = @Content(array = @ArraySchema(schema = @Schema(implementation = java.util.Map.class)))
  )
  public List<Map<String, Object>> postsRange(
    @RequestParam(value = "days", defaultValue = "30") int days
  ) {
    if (days < 1) days = 1;
    LocalDate end = LocalDate.now();
    LocalDate start = end.minusDays(days - 1L);
    var data = statService.countPostsRange(start, end);
    return data
      .entrySet()
      .stream()
      .map(e -> Map.<String, Object>of("date", e.getKey().toString(), "value", e.getValue()))
      .toList();
  }

  @GetMapping("/comments-range")
  @Operation(summary = "Comments range", description = "Get comments count over range of days")
  @ApiResponse(
    responseCode = "200",
    description = "Comment data",
    content = @Content(array = @ArraySchema(schema = @Schema(implementation = java.util.Map.class)))
  )
  public List<Map<String, Object>> commentsRange(
    @RequestParam(value = "days", defaultValue = "30") int days
  ) {
    if (days < 1) days = 1;
    LocalDate end = LocalDate.now();
    LocalDate start = end.minusDays(days - 1L);
    var data = statService.countCommentsRange(start, end);
    return data
      .entrySet()
      .stream()
      .map(e -> Map.<String, Object>of("date", e.getKey().toString(), "value", e.getValue()))
      .toList();
  }
}
