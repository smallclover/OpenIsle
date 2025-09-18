package com.openisle.repository;

import com.openisle.model.PollVote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
  List<PollVote> findByPostId(Long postId);
}
