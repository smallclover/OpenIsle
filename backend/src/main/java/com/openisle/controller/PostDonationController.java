package com.openisle.controller;

import com.openisle.dto.DonationRequest;
import com.openisle.dto.DonationResponse;
import com.openisle.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/{postId}/donations")
@RequiredArgsConstructor
public class PostDonationController {

  private final PointService pointService;

  @GetMapping
  @Operation(summary = "List donations", description = "Get recent donations for a post")
  @ApiResponse(responseCode = "200", description = "Donation summary")
  public DonationResponse list(@PathVariable Long postId) {
    return pointService.getPostDonations(postId);
  }

  @PostMapping
  @SecurityRequirement(name = "JWT")
  @Operation(summary = "Donate", description = "Donate points to the post author")
  @ApiResponse(responseCode = "200", description = "Donation result")
  public DonationResponse donate(
    @PathVariable Long postId,
    @RequestBody DonationRequest req,
    Authentication auth
  ) {
    return pointService.donateToPost(auth.getName(), postId, req.getAmount());
  }
}
