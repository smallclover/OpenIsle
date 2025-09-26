package com.openisle.search;

import com.openisle.model.Category;
import com.openisle.model.Comment;
import com.openisle.model.Post;
import com.openisle.model.Tag;
import com.openisle.model.User;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
public class SearchEntityListener {

  private static volatile OpenSearchIndexer indexer;

  public static void registerIndexer(OpenSearchIndexer openSearchIndexer) {
    indexer = openSearchIndexer;
  }

  @PostPersist
  @PostUpdate
  public void afterSave(Object entity) {
    if (indexer == null) {
      return;
    }
    if (entity instanceof Post post) {
      indexer.indexPost(post);
    } else if (entity instanceof Comment comment) {
      indexer.indexComment(comment);
    } else if (entity instanceof User user) {
      indexer.indexUser(user);
    } else if (entity instanceof Category category) {
      indexer.indexCategory(category);
    } else if (entity instanceof Tag tag) {
      indexer.indexTag(tag);
    }
  }

  @PostRemove
  public void afterDelete(Object entity) {
    if (indexer == null) {
      return;
    }
    if (entity instanceof Post post) {
      Long id = post.getId();
      if (id != null) {
        indexer.deletePost(id);
      }
    } else if (entity instanceof Comment comment) {
      Long id = comment.getId();
      if (id != null) {
        indexer.deleteComment(id);
      }
    } else if (entity instanceof User user) {
      Long id = user.getId();
      if (id != null) {
        indexer.deleteUser(id);
      }
    } else if (entity instanceof Category category) {
      Long id = category.getId();
      if (id != null) {
        indexer.deleteCategory(id);
      }
    } else if (entity instanceof Tag tag) {
      Long id = tag.getId();
      if (id != null) {
        indexer.deleteTag(id);
      }
    }
  }

  @Component
  @ConditionalOnProperty(prefix = "opensearch", name = "enabled", havingValue = "true")
  public static class Registrar {

    public Registrar(OpenSearchIndexer openSearchIndexer) {
      SearchEntityListener.registerIndexer(openSearchIndexer);
    }
  }
}
