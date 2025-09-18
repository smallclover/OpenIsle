package com.openisle.controller;

import com.openisle.dto.ChannelDto;
import com.openisle.model.User;
import com.openisle.repository.UserRepository;
import com.openisle.service.ChannelService;
import com.openisle.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

  private final ChannelService channelService;
  private final MessageService messageService;
  private final UserRepository userRepository;

  private Long getCurrentUserId(Authentication auth) {
    User user = userRepository
      .findByUsername(auth.getName())
      .orElseThrow(() -> new IllegalArgumentException("User not found"));
    return user.getId();
  }

  @GetMapping
  @Operation(summary = "List channels", description = "List channels for the current user")
  @ApiResponse(
    responseCode = "200",
    description = "Channels",
    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChannelDto.class)))
  )
  @SecurityRequirement(name = "JWT")
  public List<ChannelDto> listChannels(Authentication auth) {
    return channelService.listChannels(getCurrentUserId(auth));
  }

  @PostMapping("/{channelId}/join")
  @Operation(summary = "Join channel", description = "Join a channel")
  @ApiResponse(
    responseCode = "200",
    description = "Joined channel",
    content = @Content(schema = @Schema(implementation = ChannelDto.class))
  )
  @SecurityRequirement(name = "JWT")
  public ChannelDto joinChannel(@PathVariable Long channelId, Authentication auth) {
    return channelService.joinChannel(channelId, getCurrentUserId(auth));
  }

  @GetMapping("/unread-count")
  @Operation(summary = "Unread count", description = "Get unread channel count")
  @ApiResponse(
    responseCode = "200",
    description = "Unread count",
    content = @Content(schema = @Schema(implementation = Long.class))
  )
  @SecurityRequirement(name = "JWT")
  public long unreadCount(Authentication auth) {
    return messageService.getUnreadChannelCount(getCurrentUserId(auth));
  }
}
