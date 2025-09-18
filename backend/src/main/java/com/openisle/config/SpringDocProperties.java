package com.openisle.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "springdoc.api-docs")
public class SpringDocProperties {

  private List<ServerConfig> servers = new ArrayList<>();

  @Data
  public static class ServerConfig {

    private String url;
    private String description;
  }
}
