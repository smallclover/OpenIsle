package com.openisle.repository;

import com.openisle.model.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  List<Category> findByNameContainingIgnoreCase(String keyword);

  Optional<Category> findByName(String name);
}
