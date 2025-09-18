package com.openisle.controller;

import com.openisle.dto.PointGoodDto;
import com.openisle.dto.PointRedeemRequest;
import com.openisle.mapper.PointGoodMapper;
import com.openisle.model.User;
import com.openisle.service.PointMallService;
import com.openisle.service.UserService;
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
import org.springframework.web.bind.annotation.*;

/** REST controller for point mall. */
@RestController
@RequestMapping("/api/point-goods")
@RequiredArgsConstructor
public class PointMallController {

  private final PointMallService pointMallService;
  private final UserService userService;
  private final PointGoodMapper pointGoodMapper;

  @GetMapping
  @Operation(summary = "List goods", description = "List all point goods")
  @ApiResponse(
    responseCode = "200",
    description = "List of goods",
    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PointGoodDto.class)))
  )
  public List<PointGoodDto> list() {
    return pointMallService
      .listGoods()
      .stream()
      .map(pointGoodMapper::toDto)
      .collect(Collectors.toList());
  }

  @PostMapping("/redeem")
  @Operation(summary = "Redeem good", description = "Redeem a point good")
  @ApiResponse(
    responseCode = "200",
    description = "Remaining points",
    content = @Content(schema = @Schema(implementation = java.util.Map.class))
  )
  @SecurityRequirement(name = "JWT")
  public Map<String, Integer> redeem(@RequestBody PointRedeemRequest req, Authentication auth) {
    User user = userService.findByIdentifier(auth.getName()).orElseThrow();
    int point = pointMallService.redeem(user, req.getGoodId(), req.getContact());
    return Map.of("point", point);
  }
}
