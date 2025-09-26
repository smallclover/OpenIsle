package com.openisle.search;

import com.openisle.config.OpenSearchProperties;
import com.openisle.model.Category;
import com.openisle.model.Comment;
import com.openisle.model.Post;
import com.openisle.model.Tag;
import com.openisle.model.User;
import java.io.IOException;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "opensearch", name = "enabled", havingValue = "true")
public class OpenSearchIndexer {

  private final OpenSearchClient client;
  private final OpenSearchProperties properties;

  public void indexPost(Post post) {
    runAfterCommit(() -> {
      Map<String, Object> document = new HashMap<>();
      document.put("title", post.getTitle());
      document.put("content", post.getContent());
      document.put("author", post.getAuthor() != null ? post.getAuthor().getUsername() : null);
      document.put("category", post.getCategory() != null ? post.getCategory().getName() : null);
      document.put(
        "tags",
        post.getTags() != null
          ? post.getTags().stream().map(Tag::getName).toList()
          : java.util.List.of()
      );
      document.put("status", post.getStatus() != null ? post.getStatus().name() : null);
      if (post.getCreatedAt() != null) {
        document.put("createdAt", post.getCreatedAt().atOffset(ZoneOffset.UTC));
      }
      indexDocument(properties.getPostsIndex(), post.getId(), document);
    });
  }

  public void deletePost(Long postId) {
    runAfterCommit(() -> deleteDocument(properties.getPostsIndex(), postId));
  }

  public void indexComment(Comment comment) {
    runAfterCommit(() -> {
      Map<String, Object> document = new HashMap<>();
      document.put("content", comment.getContent());
      document.put(
        "author",
        comment.getAuthor() != null ? comment.getAuthor().getUsername() : null
      );
      if (comment.getPost() != null) {
        document.put("postId", comment.getPost().getId());
        document.put("postTitle", comment.getPost().getTitle());
      }
      if (comment.getCreatedAt() != null) {
        document.put("createdAt", comment.getCreatedAt().atOffset(ZoneOffset.UTC));
      }
      indexDocument(properties.getCommentsIndex(), comment.getId(), document);
    });
  }

  public void deleteComment(Long commentId) {
    runAfterCommit(() -> deleteDocument(properties.getCommentsIndex(), commentId));
  }

  public void indexUser(User user) {
    runAfterCommit(() -> {
      Map<String, Object> document = new HashMap<>();
      document.put("username", user.getUsername());
      document.put("displayName", user.getDisplayName());
      document.put("introduction", user.getIntroduction());
      if (user.getCreatedAt() != null) {
        document.put("createdAt", user.getCreatedAt().atOffset(ZoneOffset.UTC));
      }
      indexDocument(properties.getUsersIndex(), user.getId(), document);
    });
  }

  public void deleteUser(Long userId) {
    runAfterCommit(() -> deleteDocument(properties.getUsersIndex(), userId));
  }

  public void indexCategory(Category category) {
    runAfterCommit(() -> {
      Map<String, Object> document = new HashMap<>();
      document.put("name", category.getName());
      document.put("description", category.getDescription());
      indexDocument(properties.getCategoriesIndex(), category.getId(), document);
    });
  }

  public void deleteCategory(Long categoryId) {
    runAfterCommit(() -> deleteDocument(properties.getCategoriesIndex(), categoryId));
  }

  public void indexTag(Tag tag) {
    runAfterCommit(() -> {
      Map<String, Object> document = new HashMap<>();
      document.put("name", tag.getName());
      document.put("description", tag.getDescription());
      document.put("approved", Boolean.TRUE.equals(tag.getApproved()));
      indexDocument(properties.getTagsIndex(), tag.getId(), document);
    });
  }

  public void deleteTag(Long tagId) {
    runAfterCommit(() -> deleteDocument(properties.getTagsIndex(), tagId));
  }

  private void indexDocument(String index, Long id, Map<String, Object> document) {
    if (id == null) {
      return;
    }
    try {
      IndexRequest<Map<String, Object>> request = IndexRequest.<Map<String, Object>>builder()
        .index(index)
        .id(id.toString())
        .document(document)
        .build();
      client.index(request);
    } catch (IOException e) {
      log.error("Failed to index document {} in {}", id, index, e);
    }
  }

  private void deleteDocument(String index, Long id) {
    if (id == null) {
      return;
    }
    try {
      DeleteRequest request = DeleteRequest.of(d -> d.index(index).id(id.toString()));
      client.delete(request);
    } catch (IOException e) {
      log.error("Failed to delete document {} in {}", id, index, e);
    }
  }

  private void runAfterCommit(Runnable runnable) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            runnable.run();
          }
        }
      );
    } else {
      runnable.run();
    }
  }
}
