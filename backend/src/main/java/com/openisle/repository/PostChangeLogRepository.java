package com.openisle.repository;

import com.openisle.model.Post;
import com.openisle.model.PostChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostChangeLogRepository extends JpaRepository<PostChangeLog, Long> {
    List<PostChangeLog> findByPostOrderByCreatedAtAsc(Post post);

    void deleteByPost(Post post);
}
