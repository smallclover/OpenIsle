package com.openisle.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConfig {

  public static final String EXCHANGE_NAME = "openisle-exchange";
  // 保持向后兼容的常量
  public static final String QUEUE_NAME = "notifications-queue";
  public static final String ROUTING_KEY = "notifications.routingkey";

  // 硬编码为16以匹配ShardingStrategy中的十六进制分片逻辑
  private final int queueCount = 16;

  @Value("${rabbitmq.queue.durable}")
  private boolean queueDurable;

  @PostConstruct
  public void init() {
    log.info("RabbitMQ配置初始化: 队列数量={}, 持久化={}", queueCount, queueDurable);
  }

  @Bean
  public TopicExchange exchange() {
    return new TopicExchange(EXCHANGE_NAME);
  }

  /**
   * 创建所有分片队列, 使用十六进制后缀 (0-f)
   */
  @Bean
  public List<Queue> shardedQueues() {
    log.info("开始创建分片队列 Bean...");

    List<Queue> queues = new ArrayList<>();
    for (int i = 0; i < queueCount; i++) {
      String shardKey = Integer.toHexString(i);
      String queueName = "notifications-queue-" + shardKey;
      Queue queue = new Queue(queueName, queueDurable);
      queues.add(queue);
    }

    log.info("分片队列 Bean 创建完成，总数: {}", queues.size());
    return queues;
  }

  /**
   * 创建所有分片绑定, 使用十六进制路由键 (notifications.shard.0 - notifications.shard.f)
   */
  @Bean
  public List<Binding> shardedBindings(
    TopicExchange exchange,
    @Qualifier("shardedQueues") List<Queue> shardedQueues
  ) {
    log.info("开始创建分片绑定 Bean...");
    List<Binding> bindings = new ArrayList<>();
    if (shardedQueues != null) {
      for (Queue queue : shardedQueues) {
        String queueName = queue.getName();
        String shardKey = queueName.substring("notifications-queue-".length());
        String routingKey = "notifications.shard." + shardKey;
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);
        bindings.add(binding);
      }
    }

    log.info("分片绑定 Bean 创建完成，总数: {}", bindings.size());
    return bindings;
  }

  /**
   * 保持向后兼容的单队列配置（可选）
   */
  @Bean
  public Queue legacyQueue() {
    return new Queue(QUEUE_NAME, queueDurable);
  }

  /**
   * 保持向后兼容的单队列绑定（可选）
   */
  @Bean
  public Binding legacyBinding(Queue legacyQueue, TopicExchange exchange) {
    return BindingBuilder.bind(legacyQueue).to(exchange).with(ROUTING_KEY);
  }

  @Bean
  public Jackson2JsonMessageConverter messageConverter() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    objectMapper.disable(
      com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
    );
    return new Jackson2JsonMessageConverter(objectMapper);
  }

  @Bean
  public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
    return new RabbitAdmin(connectionFactory);
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(messageConverter());
    return template;
  }

  /**
   * 使用 CommandLineRunner 确保在应用完全启动后声明队列到 RabbitMQ
   * 这样可以确保 RabbitAdmin 和所有 Bean 都已正确初始化
   */
  @Bean
  @DependsOn({ "rabbitAdmin", "shardedQueues", "exchange" })
  public CommandLineRunner queueDeclarationRunner(
    RabbitAdmin rabbitAdmin,
    @Qualifier("shardedQueues") List<Queue> shardedQueues,
    TopicExchange exchange,
    Queue legacyQueue,
    @Qualifier("shardedBindings") List<Binding> shardedBindings,
    Binding legacyBinding
  ) {
    return args -> {
      log.info("=== 开始主动声明 RabbitMQ 组件 ===");

      try {
        // 声明交换
        rabbitAdmin.declareExchange(exchange);

        // 声明分片队列 - 检查存在性
        log.info("开始检查并声明 {} 个分片队列...", shardedQueues.size());
        int successCount = 0;
        int skippedCount = 0;

        for (Queue queue : shardedQueues) {
          String queueName = queue.getName();
          try {
            // 使用 declareQueue 的返回值判断队列是否已存在
            // 如果队列已存在且配置匹配，declareQueue 会返回现有队列信息
            // 如果不匹配或不存在，会创建新队列
            rabbitAdmin.declareQueue(queue);
            successCount++;
          } catch (org.springframework.amqp.AmqpIOException e) {
            if (
              e.getMessage().contains("PRECONDITION_FAILED") && e.getMessage().contains("durable")
            ) {
              skippedCount++;
            }
          } catch (Exception e) {
            log.error("队列声明失败: {}, 错误: {}", queueName, e.getMessage());
          }
        }
        log.info(
          "分片队列处理完成: 成功 {}, 跳过 {}, 总数 {}",
          successCount,
          skippedCount,
          shardedQueues.size()
        );

        // 声明分片绑定
        log.info("开始声明 {} 个分片绑定...", shardedBindings.size());
        int bindingSuccessCount = 0;
        for (Binding binding : shardedBindings) {
          try {
            rabbitAdmin.declareBinding(binding);
            bindingSuccessCount++;
          } catch (Exception e) {
            log.error("绑定声明失败: {}", e.getMessage());
          }
        }
        log.info("分片绑定声明完成: 成功 {}/{}", bindingSuccessCount, shardedBindings.size());

        // 声明遗留队列和绑定 - 检查存在性
        try {
          rabbitAdmin.declareQueue(legacyQueue);
          rabbitAdmin.declareBinding(legacyBinding);
          log.info("遗留队列和绑定就绪: {} (已存在或新创建)", QUEUE_NAME);
        } catch (org.springframework.amqp.AmqpIOException e) {
          if (
            e.getMessage().contains("PRECONDITION_FAILED") && e.getMessage().contains("durable")
          ) {
            log.warn("遗留队列已存在但 durable 设置不匹配: {}, 保持现有队列", QUEUE_NAME);
          } else {
            log.error("遗留队列声明失败: {}, 错误: {}", QUEUE_NAME, e.getMessage());
          }
        } catch (Exception e) {
          log.error("遗留队列声明失败: {}, 错误: {}", QUEUE_NAME, e.getMessage());
        }

        log.info("=== RabbitMQ 组件声明完成 ===");
        log.info("请检查 RabbitMQ 管理界面确认队列已正确创建");
      } catch (Exception e) {
        log.error("RabbitMQ 组件声明过程中发生严重错误", e);
      }
    };
  }
}
