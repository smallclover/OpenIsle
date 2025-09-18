package com.openisle.repository;

import com.openisle.model.ContributorConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContributorConfigRepository extends JpaRepository<ContributorConfig, Long> {
  Optional<ContributorConfig> findByUserIname(String userIname);
}
