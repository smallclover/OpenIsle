package com.openisle.repository;

import com.openisle.model.Image;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for images stored on COS.
 */
public interface ImageRepository extends JpaRepository<Image, Long> {
  Optional<Image> findByUrl(String url);
}
