package com.openisle.search;

import com.openisle.config.OpenSearchProperties;
import com.openisle.service.SearchService.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.MultiMatchQueryType;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.MsearchRequest;
import org.opensearch.client.opensearch.core.MsearchResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.HighlightField;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "opensearch", name = "enabled", havingValue = "true")
public class OpenSearchGateway {

  private final OpenSearchClient client;
  private final OpenSearchProperties properties;

  public enum PostSearchMode {
    TITLE_AND_CONTENT,
    TITLE_ONLY,
    CONTENT_ONLY,
  }

  public List<Long> searchUserIds(String keyword) {
    return searchForIds(
      properties.getUsersIndex(),
      keyword,
      List.of("username^2", "displayName^1.5", "introduction"),
      null
    );
  }

  public List<Long> searchPostIds(String keyword, PostSearchMode mode) {
    List<String> fields;
    switch (mode) {
      case TITLE_ONLY:
        fields = List.of("title^2");
        break;
      case CONTENT_ONLY:
        fields = List.of("content");
        break;
      default:
        fields = List.of("title^2", "content");
    }
    return searchForIds(
      properties.getPostsIndex(),
      keyword,
      fields,
      Query.of(q -> q.match(m -> m.field("status").query("PUBLISHED")))
    );
  }

  public List<Long> searchCommentIds(String keyword) {
    return searchForIds(
      properties.getCommentsIndex(),
      keyword,
      List.of("content", "postTitle", "author"),
      null
    );
  }

  public List<Long> searchCategoryIds(String keyword) {
    return searchForIds(
      properties.getCategoriesIndex(),
      keyword,
      List.of("name^2", "description"),
      null
    );
  }

  public List<Long> searchTagIds(String keyword) {
    return searchForIds(
      properties.getTagsIndex(),
      keyword,
      List.of("name^2", "description"),
      Query.of(q -> q.match(m -> m.field("approved").query(true)))
    );
  }

  public List<SearchResult> globalSearch(String keyword, int snippetLength) {
    try {
      MsearchRequest.Builder builder = new MsearchRequest.Builder();

      builder.searches(s ->
        s
          .header(h -> h.index(properties.getUsersIndex()))
          .body(searchBody(keyword, List.of("username^2", "displayName", "introduction"), null))
      );
      builder.searches(s ->
        s
          .header(h -> h.index(properties.getCategoriesIndex()))
          .body(searchBody(keyword, List.of("name^2", "description"), null))
      );
      builder.searches(s ->
        s
          .header(h -> h.index(properties.getTagsIndex()))
          .body(
            searchBody(
              keyword,
              List.of("name^2", "description"),
              Query.of(q -> q.match(m -> m.field("approved").query(true)))
            )
          )
      );
      builder.searches(s ->
        s
          .header(h -> h.index(properties.getPostsIndex()))
          .body(
            searchBody(
              keyword,
              List.of("title^2", "content", "category", "tags"),
              Query.of(q -> q.match(m -> m.field("status").query("PUBLISHED")))
            )
          )
      );
      builder.searches(s ->
        s
          .header(h -> h.index(properties.getCommentsIndex()))
          .body(searchBody(keyword, List.of("content", "postTitle", "author"), null))
      );

      MsearchResponse<Map<String, Object>> response = client.msearch(builder.build(), Map.class);

      List<SearchResult> results = new ArrayList<>();
      int snippetLimit = snippetLength >= 0
        ? snippetLength
        : properties.getHighlightFallbackLength();

      // Order corresponds to request order
      List<String> types = List.of("user", "category", "tag", "post", "comment");
      for (int i = 0; i < response.responses().size(); i++) {
        var item = response.responses().get(i);
        if (item.isFailure()) {
          log.warn("OpenSearch multi search failed for {}: {}", types.get(i), item.error());
          continue;
        }
        for (Hit<Map<String, Object>> hit : item.result().hits().hits()) {
          String type = types.get(i);
          Long id = hit.id() != null ? Long.valueOf(hit.id()) : null;
          Map<String, List<String>> highlight = hit.highlight() != null
            ? hit.highlight()
            : Map.of();
          Map<String, Object> source = hit.source() != null ? hit.source() : Map.of();
          String text = firstHighlight(
            highlight,
            List.of("title", "username", "name", "postTitle")
          );
          if (text == null) {
            text = optionalString(
              source,
              switch (type) {
                case "user" -> "username";
                case "post" -> "title";
                case "comment" -> "postTitle";
                default -> "name";
              }
            );
          }
          String subText = null;
          String extra = null;
          Long postId = null;

          if ("user".equals(type)) {
            subText = optionalString(source, "displayName");
            extra = snippetFromHighlight(
              highlight,
              List.of("introduction"),
              optionalString(source, "introduction"),
              snippetLimit
            );
          } else if ("category".equals(type) || "tag".equals(type)) {
            extra = snippetFromHighlight(
              highlight,
              List.of("description"),
              optionalString(source, "description"),
              snippetLimit
            );
          } else if ("post".equals(type)) {
            subText = optionalString(source, "category");
            extra = snippetFromHighlight(
              highlight,
              List.of("content"),
              optionalString(source, "content"),
              snippetLimit
            );
          } else if ("comment".equals(type)) {
            subText = optionalString(source, "author");
            postId = optionalLong(source, "postId");
            extra = snippetFromHighlight(
              highlight,
              List.of("content"),
              optionalString(source, "content"),
              snippetLimit
            );
          }

          results.add(new SearchResult(type, id, text, subText, extra, postId));
        }
      }
      return results;
    } catch (IOException e) {
      throw new IllegalStateException("OpenSearch global search failed", e);
    }
  }

  private List<Long> searchForIds(String index, String keyword, List<String> fields, Query filter) {
    try {
      SearchRequest request = SearchRequest.builder()
        .index(index)
        .size(properties.getMaxResults())
        .query(q ->
          q.bool(b -> {
            b.must(
              Query.of(m ->
                m.multiMatch(mm ->
                  mm.query(keyword).fields(fields).type(MultiMatchQueryType.BestFields)
                )
              )
            );
            if (filter != null) {
              b.filter(filter);
            }
            return b;
          })
        )
        .sort(s -> s.score(o -> o.order(SortOrder.Desc)))
        .build();
      SearchResponse<Map<String, Object>> response = client.search(request, Map.class);
      return response
        .hits()
        .hits()
        .stream()
        .map(Hit::id)
        .filter(id -> id != null && !id.isBlank())
        .map(Long::valueOf)
        .collect(Collectors.toList());
    } catch (IOException e) {
      throw new IllegalStateException("OpenSearch search failed for index " + index, e);
    }
  }

  private SearchRequest searchBody(String keyword, List<String> fields, Query filter) {
    return SearchRequest.builder()
      .size(10)
      .query(q ->
        q.bool(b -> {
          b.must(
            Query.of(m ->
              m.multiMatch(mm ->
                mm.query(keyword).fields(fields).type(MultiMatchQueryType.BestFields)
              )
            )
          );
          if (filter != null) {
            b.filter(filter);
          }
          return b;
        })
      )
      .highlight(h ->
        h
          .preTags("<em>")
          .postTags("</em>")
          .fields(
            "title",
            HighlightField.of(f -> f.fragmentSize(properties.getHighlightFallbackLength()))
          )
          .fields(
            "username",
            HighlightField.of(f -> f.fragmentSize(properties.getHighlightFallbackLength()))
          )
          .fields(
            "name",
            HighlightField.of(f -> f.fragmentSize(properties.getHighlightFallbackLength()))
          )
          .fields(
            "postTitle",
            HighlightField.of(f -> f.fragmentSize(properties.getHighlightFallbackLength()))
          )
          .fields(
            "content",
            HighlightField.of(f -> f.fragmentSize(properties.getHighlightFallbackLength()))
          )
          .fields(
            "description",
            HighlightField.of(f -> f.fragmentSize(properties.getHighlightFallbackLength()))
          )
          .fields(
            "introduction",
            HighlightField.of(f -> f.fragmentSize(properties.getHighlightFallbackLength()))
          )
      )
      .build();
  }

  private String firstHighlight(Map<String, List<String>> highlight, List<String> keys) {
    for (String key : keys) {
      List<String> values = highlight.get(key);
      if (values != null && !values.isEmpty()) {
        return values.get(0);
      }
    }
    return null;
  }

  private String snippetFromHighlight(
    Map<String, List<String>> highlight,
    List<String> keys,
    String fallback,
    int snippetLength
  ) {
    for (String key : keys) {
      List<String> values = highlight.get(key);
      if (values != null && !values.isEmpty()) {
        return values.get(0);
      }
    }
    if (fallback == null) {
      return null;
    }
    if (snippetLength < 0) {
      return fallback;
    }
    return fallback.length() > snippetLength ? fallback.substring(0, snippetLength) : fallback;
  }

  private String optionalString(Map<String, Object> source, String key) {
    if (source == null) {
      return null;
    }
    Object value = source.get(key);
    return value != null ? value.toString() : null;
  }

  private Long optionalLong(Map<String, Object> source, String key) {
    if (source == null) {
      return null;
    }
    Object value = source.get(key);
    if (value instanceof Number number) {
      return number.longValue();
    }
    if (value instanceof String str && !str.isBlank()) {
      try {
        return Long.valueOf(str);
      } catch (NumberFormatException e) {
        return null;
      }
    }
    return null;
  }
}
