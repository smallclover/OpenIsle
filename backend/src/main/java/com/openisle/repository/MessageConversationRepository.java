package com.openisle.repository;

import com.openisle.model.MessageConversation;
import com.openisle.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageConversationRepository extends JpaRepository<MessageConversation, Long> {
  @Query(
    "SELECT c FROM MessageConversation c LEFT JOIN FETCH c.participants p LEFT JOIN FETCH p.user WHERE c.id = :id"
  )
  java.util.Optional<MessageConversation> findByIdWithParticipantsAndUsers(@Param("id") Long id);

  @Query(
    "SELECT c FROM MessageConversation c " +
      "WHERE c.channel = false AND size(c.participants) = 2 " +
      "AND EXISTS (SELECT 1 FROM c.participants p1 WHERE p1.user = :user1) " +
      "AND EXISTS (SELECT 1 FROM c.participants p2 WHERE p2.user = :user2) " +
      "ORDER BY c.createdAt DESC"
  )
  List<MessageConversation> findConversationsByUsers(
    @Param("user1") User user1,
    @Param("user2") User user2
  );

  @Query(
    "SELECT DISTINCT c FROM MessageConversation c " +
      "JOIN c.participants p " +
      "LEFT JOIN FETCH c.lastMessage lm " +
      "LEFT JOIN FETCH lm.sender " +
      "LEFT JOIN FETCH c.participants cp " +
      "LEFT JOIN FETCH cp.user " +
      "WHERE p.user.id = :userId " +
      "ORDER BY COALESCE(lm.createdAt, c.createdAt) DESC"
  )
  List<MessageConversation> findConversationsByUserIdOrderByLastMessageDesc(
    @Param("userId") Long userId
  );

  List<MessageConversation> findByChannelTrue();

  long countByChannelTrue();
}
