package com.openisle.repository;

import com.openisle.model.CategoryProposalPost;
import com.openisle.model.CategoryProposalStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryProposalPostRepository extends JpaRepository<CategoryProposalPost, Long> {
  List<CategoryProposalPost> findByEndTimeAfterAndProposalStatus(
    LocalDateTime now,
    CategoryProposalStatus status
  );
  List<CategoryProposalPost> findByEndTimeBeforeAndProposalStatus(
    LocalDateTime now,
    CategoryProposalStatus status
  );
  boolean existsByProposedNameIgnoreCase(String proposedName);
}
