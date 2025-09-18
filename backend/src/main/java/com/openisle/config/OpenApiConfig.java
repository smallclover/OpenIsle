package com.openisle.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

  private final SpringDocProperties springDocProperties;

  @Value("${springdoc.info.title}")
  private String title;

  @Value("${springdoc.info.description}")
  private String description;

  @Value("${springdoc.info.version}")
  private String version;

  @Value("${springdoc.info.scheme}")
  private String scheme;

  @Value("${springdoc.info.header}")
  private String header;

  @Bean
  public OpenAPI openAPI() {
    SecurityScheme securityScheme = new SecurityScheme()
      .type(SecurityScheme.Type.HTTP)
      .scheme(scheme.toLowerCase())
      .bearerFormat("JWT")
      .in(SecurityScheme.In.HEADER)
      .name(header);

    List<Server> servers = springDocProperties
      .getServers()
      .stream()
      .map(s -> new Server().url(s.getUrl()).description(s.getDescription()))
      .collect(Collectors.toList());

    return new OpenAPI()
      .servers(servers)
      .info(new Info().title(title).description(description).version(version))
      .components(new Components().addSecuritySchemes("JWT", securityScheme))
      .addSecurityItem(new SecurityRequirement().addList("JWT"));
  }
}
