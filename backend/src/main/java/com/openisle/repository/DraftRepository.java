package com.openisle.repository;

import com.openisle.model.Draft;
import com.openisle.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DraftRepository extends JpaRepository<Draft, Long> {
  Optional<Draft> findByAuthor(User author);
  void deleteByAuthor(User author);
}
