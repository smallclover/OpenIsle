package com.openisle.repository;

import com.openisle.model.Comment;
import com.openisle.model.PointHistory;
import com.openisle.model.PointHistoryType;
import com.openisle.model.Post;
import com.openisle.model.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
  List<PointHistory> findByUserOrderByIdDesc(User user);
  List<PointHistory> findByUserOrderByIdAsc(User user);
  long countByUser(User user);

  List<PointHistory> findByUserAndCreatedAtAfterOrderByCreatedAtDesc(
    User user,
    LocalDateTime createdAt
  );

  List<PointHistory> findByComment(Comment comment);

  List<PointHistory> findByPost(Post post);

  List<PointHistory> findTop10ByPostAndTypeOrderByCreatedAtDesc(Post post, PointHistoryType type);

  @Query(
    "SELECT COALESCE(SUM(ph.amount), 0) FROM PointHistory ph WHERE ph.post = :post AND ph.type = :type"
  )
  Long sumAmountByPostAndType(@Param("post") Post post, @Param("type") PointHistoryType type);
}
