package com.openisle.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShardInfo {

  private int shardIndex;
  private String queueName;
  private String routingKey;
}
