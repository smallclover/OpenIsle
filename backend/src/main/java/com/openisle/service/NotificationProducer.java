package com.openisle.service;

import com.openisle.config.RabbitMQConfig;
import com.openisle.config.ShardInfo;
import com.openisle.config.ShardingStrategy;
import com.openisle.dto.MessageNotificationPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

  private final RabbitTemplate rabbitTemplate;
  private final ShardingStrategy shardingStrategy;

  @Value("${rabbitmq.sharding.enabled}")
  private boolean shardingEnabled;

  public void sendNotification(MessageNotificationPayload payload) {
    String targetUsername = payload.getTargetUsername();

    try {
      if (shardingEnabled) {
        // 使用分片策略发送消息
        sendShardedNotification(payload, targetUsername);
      } else {
        // 使用原始单队列方式发送（向后兼容）
        sendLegacyNotification(payload);
      }
    } catch (Exception e) {
      log.error("Failed to send message to RabbitMQ for user: {}", targetUsername, e);
      throw e;
    }
  }

  /**
   * 使用分片策略发送消息
   */
  private void sendShardedNotification(MessageNotificationPayload payload, String targetUsername) {
    ShardInfo shardInfo = shardingStrategy.getShardInfo(targetUsername);
    rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, shardInfo.getRoutingKey(), payload);
  }

  /**
   * 使用原始单队列方式发送消息（向后兼容）
   */
  private void sendLegacyNotification(MessageNotificationPayload payload) {
    rabbitTemplate.convertAndSend(
      RabbitMQConfig.EXCHANGE_NAME,
      RabbitMQConfig.ROUTING_KEY,
      payload
    );
  }
}
