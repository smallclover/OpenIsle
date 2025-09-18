package com.openisle.repository;

import com.openisle.model.Tag;
import com.openisle.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
  List<Tag> findByNameContainingIgnoreCase(String keyword);
  List<Tag> findByApproved(boolean approved);
  List<Tag> findByApprovedTrue();
  List<Tag> findByNameContainingIgnoreCaseAndApprovedTrue(String keyword);

  List<Tag> findByCreatorOrderByCreatedAtDesc(User creator, Pageable pageable);
  List<Tag> findByCreator(User creator);

  Optional<Tag> findByName(String name);
}
