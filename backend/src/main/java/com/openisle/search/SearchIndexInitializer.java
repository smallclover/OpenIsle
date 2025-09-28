package com.openisle.search;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch.indices.IndexSettings;

@Slf4j
@RequiredArgsConstructor
public class SearchIndexInitializer {

  private final OpenSearchClient client;
  private final OpenSearchProperties properties;

  @PostConstruct
  public void initialize() {
    if (!properties.isEnabled() || !properties.isInitialize()) {
      return;
    }
    ensureIndex(properties.postsIndex(), this::postMapping);
    ensureIndex(properties.commentsIndex(), this::commentMapping);
    ensureIndex(properties.usersIndex(), this::userMapping);
    ensureIndex(properties.categoriesIndex(), this::categoryMapping);
    ensureIndex(properties.tagsIndex(), this::tagMapping);
  }

  private void ensureIndex(String index, java.util.function.Supplier<TypeMapping> mappingSupplier) {
    try {
      boolean exists = client
        .indices()
        .exists(builder -> builder.index(index))
        .value();
      if (exists) {
        return;
      }
      client
        .indices()
        .create(builder ->
          builder.index(index).settings(this::applyPinyinAnalysis).mappings(mappingSupplier.get())
        );
      log.info("Created OpenSearch index {}", index);
    } catch (IOException e) {
      log.warn("Failed to initialize OpenSearch index {}", index, e);
    }
  }

  private TypeMapping postMapping() {
    return TypeMapping.of(builder ->
      builder
        .properties("type", Property.of(p -> p.keyword(k -> k)))
        .properties("title", textWithPinyin())
        .properties("content", textWithPinyin())
        .properties("author", keywordWithPinyin())
        .properties("category", keywordWithPinyin())
        .properties("tags", keywordWithPinyin())
        .properties("postId", Property.of(p -> p.long_(l -> l)))
        .properties(
          "createdAt",
          Property.of(p -> p.date(d -> d.format("strict_date_optional_time||epoch_millis")))
        )
    );
  }

  private TypeMapping commentMapping() {
    return TypeMapping.of(builder ->
      builder
        .properties("type", Property.of(p -> p.keyword(k -> k)))
        .properties("title", textWithPinyin())
        .properties("content", textWithPinyin())
        .properties("author", keywordWithPinyin())
        .properties("category", keywordWithPinyin())
        .properties("tags", keywordWithPinyin())
        .properties("postId", Property.of(p -> p.long_(l -> l)))
        .properties(
          "createdAt",
          Property.of(p -> p.date(d -> d.format("strict_date_optional_time||epoch_millis")))
        )
    );
  }

  private TypeMapping userMapping() {
    return TypeMapping.of(builder ->
      builder
        .properties("type", Property.of(p -> p.keyword(k -> k)))
        .properties("title", textWithPinyin())
        .properties("content", textWithPinyin())
        .properties(
          "createdAt",
          Property.of(p -> p.date(d -> d.format("strict_date_optional_time||epoch_millis")))
        )
    );
  }

  private TypeMapping categoryMapping() {
    return TypeMapping.of(builder ->
      builder
        .properties("type", Property.of(p -> p.keyword(k -> k)))
        .properties("title", textWithPinyin())
        .properties("content", textWithPinyin())
    );
  }

  private TypeMapping tagMapping() {
    return TypeMapping.of(builder ->
      builder
        .properties("type", Property.of(p -> p.keyword(k -> k)))
        .properties("title", textWithPinyin())
        .properties("content", textWithPinyin())
        .properties(
          "createdAt",
          Property.of(p -> p.date(d -> d.format("strict_date_optional_time||epoch_millis")))
        )
    );
  }

  private Property textWithPinyin() {
    return Property.of(p ->
      p.text(t ->
        t.fields("py", field ->
          field.text(sub -> sub.analyzer("py_index").searchAnalyzer("py_search"))
        )
      )
    );
  }

  private Property keywordWithPinyin() {
    return Property.of(p ->
      p.keyword(k ->
        k.fields("py", field ->
          field.text(sub -> sub.analyzer("py_index").searchAnalyzer("py_search"))
        )
      )
    );
  }

  private IndexSettings.Builder applyPinyinAnalysis(IndexSettings.Builder builder) {
    Map<String, JsonData> settings = new LinkedHashMap<>();
    settings.put("analysis.filter.py_filter.type", JsonData.of("pinyin"));
    settings.put("analysis.filter.py_filter.keep_full_pinyin", JsonData.of(true));
    settings.put("analysis.filter.py_filter.keep_joined_full_pinyin", JsonData.of(true));
    settings.put("analysis.filter.py_filter.keep_first_letter", JsonData.of(false));
    settings.put("analysis.filter.py_filter.remove_duplicated_term", JsonData.of(true));
    settings.put("analysis.analyzer.py_index.type", JsonData.of("custom"));
    settings.put("analysis.analyzer.py_index.tokenizer", JsonData.of("standard"));
    settings.put(
      "analysis.analyzer.py_index.filter",
      JsonData.of(List.of("lowercase", "py_filter"))
    );
    settings.put("analysis.analyzer.py_search.type", JsonData.of("custom"));
    settings.put("analysis.analyzer.py_search.tokenizer", JsonData.of("standard"));
    settings.put(
      "analysis.analyzer.py_search.filter",
      JsonData.of(List.of("lowercase", "py_filter"))
    );
    settings.forEach(builder::customSettings);
    return builder;
  }
}
