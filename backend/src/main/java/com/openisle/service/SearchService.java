package com.openisle.service;

import com.openisle.model.Category;
import com.openisle.model.Comment;
import com.openisle.model.Post;
import com.openisle.model.PostStatus;
import com.openisle.model.Tag;
import com.openisle.model.User;
import com.openisle.repository.CategoryRepository;
import com.openisle.repository.CommentRepository;
import com.openisle.repository.PostRepository;
import com.openisle.repository.TagRepository;
import com.openisle.repository.UserRepository;
import com.openisle.search.OpenSearchProperties;
import com.openisle.search.SearchDocument;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.TextQueryType;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {

  private final UserRepository userRepository;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;
  private final Optional<OpenSearchClient> openSearchClient;
  private final OpenSearchProperties openSearchProperties;

  @org.springframework.beans.factory.annotation.Value("${app.snippet-length}")
  private int snippetLength;

  private static final int DEFAULT_OPEN_SEARCH_LIMIT = 50;

  public List<User> searchUsers(String keyword) {
    return userRepository.findByUsernameContainingIgnoreCase(keyword);
  }

  public List<Post> searchPosts(String keyword) {
    return postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndStatus(
      keyword,
      keyword,
      PostStatus.PUBLISHED
    );
  }

  public List<Post> searchPostsByContent(String keyword) {
    return postRepository.findByContentContainingIgnoreCaseAndStatus(keyword, PostStatus.PUBLISHED);
  }

  public List<Post> searchPostsByTitle(String keyword) {
    return postRepository.findByTitleContainingIgnoreCaseAndStatus(keyword, PostStatus.PUBLISHED);
  }

  public List<Comment> searchComments(String keyword) {
    return commentRepository.findByContentContainingIgnoreCase(keyword);
  }

  public List<Category> searchCategories(String keyword) {
    return categoryRepository.findByNameContainingIgnoreCase(keyword);
  }

  public List<Tag> searchTags(String keyword) {
    return tagRepository.findByNameContainingIgnoreCaseAndApprovedTrue(keyword);
  }

  public List<SearchResult> globalSearch(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return List.of();
    }
    if (isOpenSearchEnabled()) {
      try {
        List<SearchResult> results = searchWithOpenSearch(keyword);
        if (!results.isEmpty()) {
          return results;
        }
      } catch (IOException e) {
        log.warn("OpenSearch global search failed, falling back to database query", e);
      }
    }
    return fallbackGlobalSearch(keyword);
  }

  private List<SearchResult> fallbackGlobalSearch(String keyword) {
    final String effectiveKeyword = keyword == null ? "" : keyword.trim();
    Stream<SearchResult> users = searchUsers(keyword)
      .stream()
      .map(u ->
        new SearchResult(
          "user",
          u.getId(),
          u.getUsername(),
          u.getIntroduction(),
          null,
          null,
          highlightHtml(u.getUsername(), effectiveKeyword),
          highlightHtml(u.getIntroduction(), effectiveKeyword),
          null
        )
      );

    Stream<SearchResult> categories = searchCategories(keyword)
      .stream()
      .map(c ->
        new SearchResult(
          "category",
          c.getId(),
          c.getName(),
          null,
          c.getDescription(),
          null,
          highlightHtml(c.getName(), effectiveKeyword),
          null,
          highlightHtml(c.getDescription(), effectiveKeyword)
        )
      );

    Stream<SearchResult> tags = searchTags(keyword)
      .stream()
      .map(t ->
        new SearchResult(
          "tag",
          t.getId(),
          t.getName(),
          null,
          t.getDescription(),
          null,
          highlightHtml(t.getName(), effectiveKeyword),
          null,
          highlightHtml(t.getDescription(), effectiveKeyword)
        )
      );

    // Merge post results while removing duplicates between search by content
    // and search by title
    List<SearchResult> mergedPosts = Stream.concat(
      searchPosts(keyword)
        .stream()
        .map(p -> {
          String snippet = extractSnippet(p.getContent(), keyword, false);
          return new SearchResult(
            "post",
            p.getId(),
            p.getTitle(),
            p.getCategory() != null ? p.getCategory().getName() : null,
            snippet,
            null,
            highlightHtml(p.getTitle(), effectiveKeyword),
            highlightHtml(
              p.getCategory() != null ? p.getCategory().getName() : null,
              effectiveKeyword
            ),
            highlightHtml(snippet, effectiveKeyword)
          );
        }),
      searchPostsByTitle(keyword)
        .stream()
        .map(p -> {
          String snippet = extractSnippet(p.getContent(), keyword, true);
          return new SearchResult(
            "post_title",
            p.getId(),
            p.getTitle(),
            p.getCategory() != null ? p.getCategory().getName() : null,
            snippet,
            null,
            highlightHtml(p.getTitle(), effectiveKeyword),
            highlightHtml(
              p.getCategory() != null ? p.getCategory().getName() : null,
              effectiveKeyword
            ),
            highlightHtml(snippet, effectiveKeyword)
          );
        })
    )
      .collect(
        java.util.stream.Collectors.toMap(
          SearchResult::id,
          sr -> sr,
          (a, b) -> a,
          java.util.LinkedHashMap::new
        )
      )
      .values()
      .stream()
      .toList();

    Stream<SearchResult> comments = searchComments(keyword)
      .stream()
      .map(c -> {
        String snippet = extractSnippet(c.getContent(), keyword, false);
        return new SearchResult(
          "comment",
          c.getId(),
          c.getPost().getTitle(),
          c.getAuthor().getUsername(),
          snippet,
          c.getPost().getId(),
          highlightHtml(c.getPost().getTitle(), effectiveKeyword),
          highlightHtml(c.getAuthor().getUsername(), effectiveKeyword),
          highlightHtml(snippet, effectiveKeyword)
        );
      });

    return Stream.of(users, categories, tags, mergedPosts.stream(), comments)
      .flatMap(s -> s)
      .toList();
  }

  private boolean isOpenSearchEnabled() {
    return openSearchProperties.isEnabled() && openSearchClient.isPresent();
  }

  // 在类里加上（字段或静态常量都可）
  private static final java.util.regex.Pattern HANS_PATTERN = java.util.regex.Pattern.compile(
    "\\p{IsHan}"
  );

  private static boolean containsHan(String s) {
    return s != null && HANS_PATTERN.matcher(s).find();
  }

  private List<SearchResult> searchWithOpenSearch(String keyword) throws IOException {
    var client = openSearchClient.orElse(null);
    if (client == null) return List.of();

    final String qRaw = keyword == null ? "" : keyword.trim();
    if (qRaw.isEmpty()) return List.of();

    final boolean hasHan = containsHan(qRaw);

    SearchResponse<SearchDocument> resp = client.search(
      b ->
        b
          .index(searchIndices())
          .trackTotalHits(t -> t.enabled(true))
          .query(qb ->
            qb.bool(bool -> {
              // ---------- 严格层 ----------
              // 中文/任意短语（轻微符号/空白扰动）
              bool.should(s ->
                s.matchPhrase(mp -> mp.field("title").query(qRaw).slop(2).boost(6.0f))
              );
              bool.should(s ->
                s.matchPhrase(mp -> mp.field("content").query(qRaw).slop(2).boost(2.5f))
              );

              // 结构化等值（.raw）
              bool.should(s ->
                s.term(t ->
                  t
                    .field("author.raw")
                    .value(v -> v.stringValue(qRaw))
                    .boost(4.0f)
                )
              );
              bool.should(s ->
                s.term(t ->
                  t
                    .field("category.raw")
                    .value(v -> v.stringValue(qRaw))
                    .boost(3.0f)
                )
              );
              bool.should(s ->
                s.term(t ->
                  t
                    .field("tags.raw")
                    .value(v -> v.stringValue(qRaw))
                    .boost(3.0f)
                )
              );

              // 拼音短语（严格）
              bool.should(s ->
                s.matchPhrase(mp -> mp.field("title.py").query(qRaw).slop(1).boost(4.0f))
              );
              bool.should(s ->
                s.matchPhrase(mp -> mp.field("content.py").query(qRaw).slop(1).boost(1.8f))
              );
              bool.should(s ->
                s.matchPhrase(mp -> mp.field("author.py").query(qRaw).slop(1).boost(2.2f))
              );
              bool.should(s ->
                s.matchPhrase(mp -> mp.field("category.py").query(qRaw).slop(1).boost(2.0f))
              );
              bool.should(s ->
                s.matchPhrase(mp -> mp.field("tags.py").query(qRaw).slop(1).boost(2.0f))
              );

              // ---------- 放宽层（仅当包含中文时启用） ----------
              if (hasHan) {
                // title.zh
                bool.should(s ->
                  s.match(m ->
                    m
                      .field("title.zh")
                      .query(org.opensearch.client.opensearch._types.FieldValue.of(qRaw))
                      .operator(org.opensearch.client.opensearch._types.query_dsl.Operator.Or)
                      .minimumShouldMatch("2<-1 3<-1 4<-1 5<-2 6<-2 7<-3")
                      .boost(3.0f)
                  )
                );
                // content.zh
                bool.should(s ->
                  s.match(m ->
                    m
                      .field("content.zh")
                      .query(org.opensearch.client.opensearch._types.FieldValue.of(qRaw))
                      .operator(org.opensearch.client.opensearch._types.query_dsl.Operator.Or)
                      .minimumShouldMatch("2<-1 3<-1 4<-1 5<-2 6<-2 7<-3")
                      .boost(1.6f)
                  )
                );
              }

              return bool.minimumShouldMatch("1");
            })
          )
          // ---------- 高亮：允许跨子字段回填 + 匹配字段组 ----------
          .highlight(h -> {
            var hb = h
              .preTags("<mark>")
              .postTags("</mark>")
              .requireFieldMatch(false)
              .fields("title", f ->
                f
                  .fragmentSize(highlightFragmentSize())
                  .numberOfFragments(1)
                  .matchedFields(List.of("title", "title.zh", "title.py"))
              )
              .fields("content", f ->
                f
                  .fragmentSize(highlightFragmentSize())
                  .numberOfFragments(1)
                  .matchedFields(List.of("content", "content.zh", "content.py"))
              )
              .fields("title.zh", f -> f.fragmentSize(highlightFragmentSize()).numberOfFragments(1))
              .fields("content.zh", f ->
                f.fragmentSize(highlightFragmentSize()).numberOfFragments(1)
              )
              .fields("title.py", f -> f.fragmentSize(highlightFragmentSize()).numberOfFragments(1))
              .fields("content.py", f ->
                f.fragmentSize(highlightFragmentSize()).numberOfFragments(1)
              )
              .fields("author", f -> f.numberOfFragments(0))
              .fields("author.py", f -> f.numberOfFragments(0))
              .fields("category", f -> f.numberOfFragments(0))
              .fields("category.py", f -> f.numberOfFragments(0))
              .fields("tags", f -> f.numberOfFragments(0))
              .fields("tags.py", f -> f.numberOfFragments(0));
            return hb;
          })
          .size(DEFAULT_OPEN_SEARCH_LIMIT > 0 ? DEFAULT_OPEN_SEARCH_LIMIT : 10),
      SearchDocument.class
    );

    return mapHits(resp.hits().hits(), qRaw);
  }

  /** Lucene query_string 安全转义（保留 * 由我们自己追加） */
  private static String escapeForQueryString(String s) {
    if (s == null || s.isEmpty()) return "";
    StringBuilder sb = new StringBuilder(s.length() * 2);
    for (char c : s.toCharArray()) {
      switch (c) {
        case '+':
        case '-':
        case '=':
        case '&':
        case '|':
        case '>':
        case '<':
        case '!':
        case '(':
        case ')':
        case '{':
        case '}':
        case '[':
        case ']':
        case '^':
        case '"':
        case '~': /* case '*': */ /* case '?': */
        case ':':
        case '\\':
        case '/':
          sb.append('\\').append(c);
          break;
        default:
          sb.append(c);
      }
    }
    return sb.toString();
  }

  private int highlightFragmentSize() {
    int configured = openSearchProperties.getHighlightFragmentSize();
    if (configured > 0) {
      return configured;
    }
    if (snippetLength > 0) {
      return snippetLength;
    }
    return 200;
  }

  private List<String> searchIndices() {
    return List.of(
      openSearchProperties.postsIndex(),
      openSearchProperties.commentsIndex(),
      openSearchProperties.usersIndex(),
      openSearchProperties.categoriesIndex(),
      openSearchProperties.tagsIndex()
    );
  }

  private List<SearchResult> mapHits(List<Hit<SearchDocument>> hits, String keyword) {
    List<SearchResult> results = new ArrayList<>();
    for (Hit<SearchDocument> hit : hits) {
      SearchResult result = mapHit(hit, keyword);
      if (result != null) {
        results.add(result);
      }
    }
    return results;
  }

  private SearchResult mapHit(Hit<SearchDocument> hit, String keyword) {
    SearchDocument document = hit.source();
    if (document == null || document.entityId() == null) {
      return null;
    }
    Map<String, List<String>> highlight = hit.highlight();
    String highlightedContent = firstHighlight(
      highlight,
      "content",
      "content.py",
      "content.zh",
      "content.raw"
    );
    String highlightedTitle = firstHighlight(
      highlight,
      "title",
      "title.py",
      "title.zh",
      "title.raw"
    );
    String highlightedAuthor = firstHighlight(highlight, "author", "author.py");
    String highlightedCategory = firstHighlight(highlight, "category", "category.py");
    boolean highlightTitle = highlightedTitle != null && !highlightedTitle.isBlank();
    String documentType = document.type() != null ? document.type() : "";
    String effectiveType = documentType;
    if ("post".equals(documentType) && highlightTitle) {
      effectiveType = "post_title";
    }
    String snippetHtml = highlightedContent != null && !highlightedContent.isBlank()
      ? highlightedContent
      : null;
    if (snippetHtml == null && highlightTitle) {
      snippetHtml = highlightedTitle;
    }

    String snippet = snippetHtml != null && !snippetHtml.isBlank()
      ? cleanHighlight(snippetHtml)
      : null;
    boolean fromStart = "post_title".equals(effectiveType);
    if (snippet == null || snippet.isBlank()) {
      snippet = fallbackSnippet(document.content(), keyword, fromStart);
      if (snippetHtml == null) {
        snippetHtml = highlightHtml(snippet, keyword);
      }
    } else if (snippetHtml == null) {
      snippetHtml = highlightHtml(snippet, keyword);
    }
    if (snippet == null) {
      snippet = "";
    }
    String subText = null;
    Long postId = null;
    if ("post".equals(documentType) || "post_title".equals(effectiveType)) {
      subText = document.category();
    } else if ("comment".equals(documentType)) {
      subText = document.author();
      postId = document.postId();
    }
    String highlightedText = highlightTitle
      ? highlightedTitle
      : highlightHtml(document.title(), keyword);
    String highlightedSubText;
    if ("comment".equals(documentType)) {
      highlightedSubText = highlightedAuthor != null && !highlightedAuthor.isBlank()
        ? highlightedAuthor
        : highlightHtml(subText, keyword);
    } else if ("post".equals(documentType) || "post_title".equals(effectiveType)) {
      highlightedSubText = highlightedCategory != null && !highlightedCategory.isBlank()
        ? highlightedCategory
        : highlightHtml(subText, keyword);
    } else {
      highlightedSubText = highlightHtml(subText, keyword);
    }
    String highlightedExtra = snippetHtml != null ? snippetHtml : highlightHtml(snippet, keyword);
    return new SearchResult(
      effectiveType,
      document.entityId(),
      document.title(),
      subText,
      snippet,
      postId,
      highlightedText,
      highlightedSubText,
      highlightedExtra
    );
  }

  private String firstHighlight(Map<String, List<String>> highlight, String... fields) {
    if (highlight == null || fields == null) {
      return null;
    }
    for (String field : fields) {
      if (field == null) {
        continue;
      }
      List<String> values = highlight.get(field);
      if (values == null || values.isEmpty()) {
        continue;
      }
      for (String value : values) {
        if (value != null && !value.isBlank()) {
          return value;
        }
      }
    }
    return null;
  }

  private String cleanHighlight(String value) {
    if (value == null) {
      return null;
    }
    return value.replaceAll("<[^>]+>", "");
  }

  private String fallbackSnippet(String content, String keyword, boolean fromStart) {
    if (content == null) {
      return "";
    }
    return extractSnippet(content, keyword, fromStart);
  }

  private String extractSnippet(String content, String keyword, boolean fromStart) {
    if (content == null) return "";
    int limit = snippetLength;
    if (fromStart) {
      if (limit < 0) {
        return content;
      }
      return content.length() > limit ? content.substring(0, limit) : content;
    }
    String lower = content.toLowerCase();
    String kw = keyword.toLowerCase();
    int idx = lower.indexOf(kw);
    if (idx == -1) {
      if (limit < 0) {
        return content;
      }
      return content.length() > limit ? content.substring(0, limit) : content;
    }
    int start = Math.max(0, idx - 20);
    int end = Math.min(content.length(), idx + kw.length() + 20);
    String snippet = content.substring(start, end);
    if (limit >= 0 && snippet.length() > limit) {
      snippet = snippet.substring(0, limit);
    }
    return snippet;
  }

  private String highlightHtml(String text, String keyword) {
    if (text == null) {
      return null;
    }
    String normalizedKeyword = keyword == null ? "" : keyword.trim();
    if (normalizedKeyword.isEmpty()) {
      return HtmlUtils.htmlEscape(text);
    }
    Pattern pattern = Pattern.compile(
      Pattern.quote(normalizedKeyword),
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    Matcher matcher = pattern.matcher(text);
    if (!matcher.find()) {
      return HtmlUtils.htmlEscape(text);
    }
    matcher.reset();
    StringBuilder sb = new StringBuilder();
    int lastEnd = 0;
    while (matcher.find()) {
      sb.append(HtmlUtils.htmlEscape(text.substring(lastEnd, matcher.start())));
      sb.append("<mark>");
      sb.append(HtmlUtils.htmlEscape(matcher.group()));
      sb.append("</mark>");
      lastEnd = matcher.end();
    }
    sb.append(HtmlUtils.htmlEscape(text.substring(lastEnd)));
    return sb.toString();
  }

  public record SearchResult(
    String type,
    Long id,
    String text,
    String subText,
    String extra,
    Long postId,
    String highlightedText,
    String highlightedSubText,
    String highlightedExtra
  ) {}
}
