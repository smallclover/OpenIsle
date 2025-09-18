package com.openisle.controller;

import com.openisle.config.CachingConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * @author smallclover
 * @since 2025-09-05
 * 统计在线人数
 */
@RestController
@RequestMapping("/api/online")
@RequiredArgsConstructor
public class OnlineController {

  private final RedisTemplate redisTemplate;
  private static final String ONLINE_KEY = CachingConfig.ONLINE_CACHE_NAME + ":";

  @PostMapping("/heartbeat")
  @Operation(summary = "Heartbeat", description = "Record user heartbeat")
  @ApiResponse(responseCode = "200", description = "Heartbeat recorded")
  public void ping(@RequestParam String userId) {
    redisTemplate.opsForValue().set(ONLINE_KEY + userId, "1", Duration.ofSeconds(150));
  }

  @GetMapping("/count")
  @Operation(summary = "Online count", description = "Get current online user count")
  @ApiResponse(
    responseCode = "200",
    description = "Online count",
    content = @Content(schema = @Schema(implementation = Long.class))
  )
  public long count() {
    return redisTemplate.keys(ONLINE_KEY + "*").size();
  }
}
