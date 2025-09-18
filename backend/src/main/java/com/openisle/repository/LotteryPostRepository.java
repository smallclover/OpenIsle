package com.openisle.repository;

import com.openisle.model.LotteryPost;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LotteryPostRepository extends JpaRepository<LotteryPost, Long> {
  List<LotteryPost> findByEndTimeAfterAndWinnersIsEmpty(LocalDateTime now);

  List<LotteryPost> findByEndTimeBeforeAndWinnersIsEmpty(LocalDateTime now);
}
