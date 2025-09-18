package com.openisle.controller;

import com.openisle.dto.ActivityDto;
import com.openisle.dto.MilkTeaInfoDto;
import com.openisle.dto.MilkTeaRedeemRequest;
import com.openisle.mapper.ActivityMapper;
import com.openisle.model.Activity;
import com.openisle.model.ActivityType;
import com.openisle.model.User;
import com.openisle.service.ActivityService;
import com.openisle.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

  private final ActivityService activityService;
  private final UserService userService;
  private final ActivityMapper activityMapper;

  @GetMapping
  @Operation(summary = "List activities", description = "Retrieve all activities")
  @ApiResponse(
    responseCode = "200",
    description = "List of activities",
    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ActivityDto.class)))
  )
  public List<ActivityDto> list() {
    return activityService.list().stream().map(activityMapper::toDto).collect(Collectors.toList());
  }

  @GetMapping("/milk-tea")
  @Operation(summary = "Milk tea info", description = "Get milk tea activity information")
  @ApiResponse(
    responseCode = "200",
    description = "Milk tea info",
    content = @Content(schema = @Schema(implementation = MilkTeaInfoDto.class))
  )
  public MilkTeaInfoDto milkTea() {
    Activity a = activityService.getByType(ActivityType.MILK_TEA);
    long count = activityService.countParticipants(a);
    if (!a.isEnded() && count >= 50) {
      activityService.end(a);
    }
    MilkTeaInfoDto info = new MilkTeaInfoDto();
    info.setRedeemCount(count);
    info.setEnded(a.isEnded());
    return info;
  }

  @PostMapping("/milk-tea/redeem")
  @Operation(summary = "Redeem milk tea", description = "Redeem milk tea activity reward")
  @ApiResponse(
    responseCode = "200",
    description = "Redeem result",
    content = @Content(schema = @Schema(implementation = java.util.Map.class))
  )
  @SecurityRequirement(name = "JWT")
  public java.util.Map<String, String> redeemMilkTea(
    @RequestBody MilkTeaRedeemRequest req,
    Authentication auth
  ) {
    User user = userService.findByIdentifier(auth.getName()).orElseThrow();
    Activity a = activityService.getByType(ActivityType.MILK_TEA);
    boolean first = activityService.redeem(a, user, req.getContact());
    if (first) {
      return java.util.Map.of("message", "redeemed");
    }
    return java.util.Map.of("message", "updated");
  }
}
