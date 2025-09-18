package com.openisle.repository;

import com.openisle.model.InviteToken;
import com.openisle.model.User;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteTokenRepository extends JpaRepository<InviteToken, String> {
  Optional<InviteToken> findByInviterAndCreatedDate(User inviter, LocalDate createdDate);

  Optional<InviteToken> findByShortToken(String shortToken);

  boolean existsByShortToken(String shortToken);
}
