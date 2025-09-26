package com.openisle.search;

import com.openisle.config.OpenSearchProperties;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.opensearch.indices.IndexSettings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "opensearch", name = "enabled", havingValue = "true")
public class OpenSearchIndexManager {

  private final OpenSearchClient client;
  private final OpenSearchProperties properties;

  @EventListener(ContextRefreshedEvent.class)
  public void initializeIndices() {
    ensureIndex(properties.getPostsIndex(), this::postsMapping);
    ensureIndex(properties.getCommentsIndex(), this::commentsMapping);
    ensureIndex(properties.getUsersIndex(), this::usersMapping);
    ensureIndex(properties.getCategoriesIndex(), this::categoriesMapping);
    ensureIndex(properties.getTagsIndex(), this::tagsMapping);
  }

  private void ensureIndex(String indexName, MappingSupplier supplier) {
    try {
      boolean exists = client.indices().exists(ExistsRequest.of(e -> e.index(indexName))).value();
      if (!exists) {
        log.info("Creating OpenSearch index {}", indexName);
        CreateIndexRequest request = CreateIndexRequest.builder()
          .index(indexName)
          .mappings(supplier.mapping())
          .settings(IndexSettings.of(s -> s.numberOfReplicas("1").numberOfShards("1")))
          .build();
        client.indices().create(request);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to ensure index " + indexName, e);
    }
  }

  private TypeMapping postsMapping() {
    return TypeMapping.builder()
      .properties(
        "title",
        Property.of(p -> p.text(t -> t.analyzer("standard").fields("raw", f -> f.keyword(k -> k))))
      )
      .properties("content", Property.of(p -> p.text(t -> t.analyzer("standard"))))
      .properties("author", Property.of(p -> p.keyword(k -> k)))
      .properties("category", Property.of(p -> p.keyword(k -> k)))
      .properties("tags", Property.of(p -> p.keyword(k -> k)))
      .properties("status", Property.of(p -> p.keyword(k -> k)))
      .properties("createdAt", Property.of(p -> p.date(d -> d)))
      .build();
  }

  private TypeMapping commentsMapping() {
    return TypeMapping.builder()
      .properties("content", Property.of(p -> p.text(t -> t.analyzer("standard"))))
      .properties("author", Property.of(p -> p.keyword(k -> k)))
      .properties("postTitle", Property.of(p -> p.text(t -> t.analyzer("standard"))))
      .properties("postId", Property.of(p -> p.long_(l -> l)))
      .properties("createdAt", Property.of(p -> p.date(d -> d)))
      .build();
  }

  private TypeMapping usersMapping() {
    return TypeMapping.builder()
      .properties(
        "username",
        Property.of(p -> p.text(t -> t.analyzer("standard").fields("raw", f -> f.keyword(k -> k))))
      )
      .properties("introduction", Property.of(p -> p.text(t -> t.analyzer("standard"))))
      .properties("displayName", Property.of(p -> p.text(t -> t.analyzer("standard"))))
      .properties("createdAt", Property.of(p -> p.date(d -> d)))
      .build();
  }

  private TypeMapping categoriesMapping() {
    return TypeMapping.builder()
      .properties(
        "name",
        Property.of(p -> p.text(t -> t.analyzer("standard").fields("raw", f -> f.keyword(k -> k))))
      )
      .properties("description", Property.of(p -> p.text(t -> t.analyzer("standard"))))
      .build();
  }

  private TypeMapping tagsMapping() {
    return TypeMapping.builder()
      .properties(
        "name",
        Property.of(p -> p.text(t -> t.analyzer("standard").fields("raw", f -> f.keyword(k -> k))))
      )
      .properties("description", Property.of(p -> p.text(t -> t.analyzer("standard"))))
      .properties("approved", Property.of(p -> p.boolean_(b -> b)))
      .build();
  }

  @FunctionalInterface
  private interface MappingSupplier {
    TypeMapping mapping();
  }
}
