package com.openisle.config;

import java.time.Duration;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "opensearch")
public class OpenSearchProperties {

  /**
   * Flag to enable the OpenSearch integration. When disabled the application falls back to the
   * legacy JPA based search implementation.
   */
  private boolean enabled = false;

  /**
   * Comma separated list of OpenSearch endpoints. Example: {@code https://localhost:9200}.
   */
  private List<String> hosts = List.of();

  /** Username used when authenticating against the cluster. */
  private String username;

  /** Password used when authenticating against the cluster. */
  private String password;

  /** Optional toggle that allows disabling certificate validation in development environments. */
  private boolean insecure = false;

  /** Connection timeout when communicating with OpenSearch. */
  private Duration connectTimeout = Duration.ofSeconds(10);

  /** Socket timeout when communicating with OpenSearch. */
  private Duration socketTimeout = Duration.ofSeconds(30);

  /** Maximum number of search results returned for entity specific endpoints. */
  private int maxResults = 50;

  /** Highlight fragment size used when OpenSearch does not return highlighted text. */
  private int highlightFallbackLength = 200;

  public String getPostsIndex() {
    return "posts";
  }

  public String getCommentsIndex() {
    return "comments";
  }

  public String getUsersIndex() {
    return "users";
  }

  public String getCategoriesIndex() {
    return "categories";
  }

  public String getTagsIndex() {
    return "tags";
  }
}
