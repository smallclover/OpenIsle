package com.openisle.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Logs a message when a Redis connection is successfully established.
 */
@Component
@Slf4j
public class RedisConnectionLogger implements InitializingBean {

  private final RedisConnectionFactory connectionFactory;

  public RedisConnectionLogger(RedisConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  @Override
  public void afterPropertiesSet() {
    try (var connection = connectionFactory.getConnection()) {
      connection.ping();
      if (connectionFactory instanceof LettuceConnectionFactory lettuce) {
        log.info("Redis connection established at {}:{}", lettuce.getHostName(), lettuce.getPort());
      } else {
        log.info("Redis connection established");
      }
    } catch (Exception e) {
      log.error("Failed to connect to Redis", e);
    }
  }
}
