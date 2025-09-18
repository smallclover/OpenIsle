package com.openisle.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShardingStrategy {

  // 固定为16以匹配RabbitMQConfig中的十六进制分片逻辑
  private static final int QUEUE_COUNT = 16;

  // 分片分布统计
  private final Map<Integer, AtomicLong> shardCounts = new ConcurrentHashMap<>();

  /**
   * 根据用户名获取分片信息（基于哈希值首字符）
   */
  public ShardInfo getShardInfo(String username) {
    if (username == null || username.isEmpty()) {
      // 空用户名默认分到第0个分片
      return getShardInfoByIndex(0);
    }

    // 计算用户名的哈希值并转为十六进制字符串
    String hash = Integer.toHexString(Math.abs(username.hashCode()));

    // 取哈希值的第一个字符 (0-9, a-f)
    char firstChar = hash.charAt(0);

    // 十六进制字符映射到队列
    int shard = getShardFromHexChar(firstChar);
    recordShardUsage(shard);

    log.debug(
      "Username '{}' -> hash '{}' -> firstChar '{}' -> shard {}",
      username,
      hash,
      firstChar,
      shard
    );

    return getShardInfoByIndex(shard);
  }

  /**
   * 将十六进制字符映射到分片索引
   */
  private int getShardFromHexChar(char hexChar) {
    int charValue;
    if (hexChar >= '0' && hexChar <= '9') {
      charValue = hexChar - '0'; // 0-9
    } else if (hexChar >= 'a' && hexChar <= 'f') {
      charValue = hexChar - 'a' + 10; // 10-15
    } else {
      // 异常情况，默认为0
      charValue = 0;
    }

    // 映射到队列数量范围内
    return charValue % QUEUE_COUNT;
  }

  /**
   * 根据分片索引获取分片信息
   */
  private ShardInfo getShardInfoByIndex(int shard) {
    String shardKey = Integer.toHexString(shard);
    return new ShardInfo(
      shard,
      "notifications-queue-" + shardKey,
      "notifications.shard." + shardKey
    );
  }

  /**
   * 记录分片使用统计
   */
  private void recordShardUsage(int shard) {
    shardCounts.computeIfAbsent(shard, k -> new AtomicLong(0)).incrementAndGet();
  }
}
