package com.openisle.repository;

import com.openisle.model.MessageParticipant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageParticipantRepository extends JpaRepository<MessageParticipant, Long> {
  Optional<MessageParticipant> findByConversationIdAndUserId(Long conversationId, Long userId);
  List<MessageParticipant> findByUserId(Long userId);
}
