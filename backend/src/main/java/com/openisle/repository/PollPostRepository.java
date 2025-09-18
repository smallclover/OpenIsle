package com.openisle.repository;

import com.openisle.model.PollPost;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollPostRepository extends JpaRepository<PollPost, Long> {
  List<PollPost> findByEndTimeAfterAndResultAnnouncedFalse(LocalDateTime now);

  List<PollPost> findByEndTimeBeforeAndResultAnnouncedFalse(LocalDateTime now);
}
