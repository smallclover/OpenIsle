package com.openisle.repository;

import com.openisle.model.PointHistory;
import com.openisle.model.User;
import com.openisle.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    List<PointHistory> findByUserOrderByIdDesc(User user);
    List<PointHistory> findByUserOrderByIdAsc(User user);
    long countByUser(User user);

    List<PointHistory> findByUserAndCreatedAtAfterOrderByCreatedAtDesc(User user, LocalDateTime createdAt);
    
    List<PointHistory> findByComment(Comment comment);
}
