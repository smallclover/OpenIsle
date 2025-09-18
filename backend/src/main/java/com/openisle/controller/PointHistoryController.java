package com.openisle.controller;

import com.openisle.dto.PointHistoryDto;
import com.openisle.mapper.PointHistoryMapper;
import com.openisle.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/point-histories")
@RequiredArgsConstructor
public class PointHistoryController {

  private final PointService pointService;
  private final PointHistoryMapper pointHistoryMapper;

  @GetMapping
  @Operation(summary = "Point history", description = "List point history for current user")
  @ApiResponse(
    responseCode = "200",
    description = "List of point histories",
    content = @Content(
      array = @ArraySchema(schema = @Schema(implementation = PointHistoryDto.class))
    )
  )
  @SecurityRequirement(name = "JWT")
  public List<PointHistoryDto> list(Authentication auth) {
    return pointService
      .listHistory(auth.getName())
      .stream()
      .map(pointHistoryMapper::toDto)
      .collect(Collectors.toList());
  }

  @GetMapping("/trend")
  @Operation(summary = "Point trend", description = "Get point trend data for current user")
  @ApiResponse(
    responseCode = "200",
    description = "Trend data",
    content = @Content(array = @ArraySchema(schema = @Schema(implementation = java.util.Map.class)))
  )
  @SecurityRequirement(name = "JWT")
  public List<Map<String, Object>> trend(
    Authentication auth,
    @RequestParam(value = "days", defaultValue = "30") int days
  ) {
    return pointService.trend(auth.getName(), days);
  }
}
