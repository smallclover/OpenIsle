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

  // SearchIndexInitializer.java —— 只贴需要替换/新增的方法

  private TypeMapping postMapping() {
    return TypeMapping.of(builder ->
      builder
        .properties("type", Property.of(p -> p.keyword(k -> k)))
        .properties("title", textWithRawAndPinyin())
        .properties("content", textWithPinyinOnly()) // content 不做 .raw，避免超长 keyword
        .properties("author", keywordWithRawAndPinyin())
        .properties("category", keywordWithRawAndPinyin())
        .properties("tags", keywordWithRawAndPinyin())
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
        .properties("title", textWithRawAndPinyin())
        .properties("content", textWithPinyinOnly())
        .properties("author", keywordWithRawAndPinyin())
        .properties("category", keywordWithRawAndPinyin())
        .properties("tags", keywordWithRawAndPinyin())
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
        .properties("title", textWithRawAndPinyin())
        .properties("content", textWithPinyinOnly())
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
        .properties("title", textWithRawAndPinyin())
        .properties("content", textWithPinyinOnly())
    );
  }

  private TypeMapping tagMapping() {
    return TypeMapping.of(builder ->
      builder
        .properties("type", Property.of(p -> p.keyword(k -> k)))
        .properties("title", textWithRawAndPinyin())
        .properties("content", textWithPinyinOnly())
        .properties(
          "createdAt",
          Property.of(p -> p.date(d -> d.format("strict_date_optional_time||epoch_millis")))
        )
    );
  }

  // SearchIndexInitializer.java —— 只贴需要替换/新增的方法

  /** 文本字段：.raw（keyword 精确） + .py（拼音短语精确） + .zh（ICU+2~3gram 召回） */
  private Property textWithRawAndPinyin() {
    return Property.of(p ->
      p.text(t ->
        t
          .fields("raw", f -> f.keyword(k -> k.normalizer("lowercase_normalizer")))
          .fields("py", f -> f.text(sub -> sub.analyzer("py_index").searchAnalyzer("py_search")))
          .fields("zh", f ->
            f.text(sub -> sub.analyzer("zh_ngram_index").searchAnalyzer("zh_search"))
          )
      )
    );
  }

  /** 长文本 content：保留拼音 + 新增 zh 子字段（不加 .raw，避免过长 keyword） */
  private Property textWithPinyinOnly() {
    return Property.of(p ->
      p.text(t ->
        t
          .fields("py", f -> f.text(sub -> sub.analyzer("py_index").searchAnalyzer("py_search")))
          .fields("zh", f ->
            f.text(sub -> sub.analyzer("zh_ngram_index").searchAnalyzer("zh_search"))
          )
      )
    );
  }

  /** 关键词字段（author/category/tags）：keyword 等值 + .py + .zh（尽量对齐标题策略） */
  private Property keywordWithRawAndPinyin() {
    return Property.of(p ->
      p.keyword(k ->
        k
          .normalizer("lowercase_normalizer")
          .fields("raw", f -> f.keyword(kk -> kk.normalizer("lowercase_normalizer")))
          .fields("py", f -> f.text(sub -> sub.analyzer("py_index").searchAnalyzer("py_search")))
          .fields("zh", f ->
            f.text(sub -> sub.analyzer("zh_ngram_index").searchAnalyzer("zh_search"))
          )
      )
    );
  }

  /** 新增 zh 分析器（ICU + 2~3gram），并保留你已有的 pinyin/normalizer 设置 */
  private IndexSettings.Builder applyPinyinAnalysis(IndexSettings.Builder builder) {
    Map<String, JsonData> settings = new LinkedHashMap<>();

    // --- 已有：keyword normalizer（用于 .raw）
    settings.put("analysis.normalizer.lowercase_normalizer.type", JsonData.of("custom"));
    settings.put(
      "analysis.normalizer.lowercase_normalizer.filter",
      JsonData.of(List.of("lowercase"))
    );

    // --- 已有：pinyin filter + analyzers
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

    settings.put("analysis.filter.zh_ngram_2_3.type", JsonData.of("ngram"));
    settings.put("analysis.filter.zh_ngram_2_3.min_gram", JsonData.of(2));
    settings.put("analysis.filter.zh_ngram_2_3.max_gram", JsonData.of(3));

    settings.put("analysis.analyzer.zh_ngram_index.type", JsonData.of("custom"));
    settings.put("analysis.analyzer.zh_ngram_index.tokenizer", JsonData.of("icu_tokenizer"));
    settings.put(
      "analysis.analyzer.zh_ngram_index.filter",
      JsonData.of(List.of("lowercase", "zh_ngram_2_3"))
    );

    settings.put("analysis.analyzer.zh_search.type", JsonData.of("custom"));
    settings.put("analysis.analyzer.zh_search.tokenizer", JsonData.of("icu_tokenizer"));
    settings.put(
      "analysis.analyzer.zh_search.filter",
      JsonData.of(List.of("lowercase", "zh_ngram_2_3"))
    );

    settings.forEach(builder::customSettings);
    return builder;
  }
}
