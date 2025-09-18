package com.openisle.repository;

import com.openisle.model.Message;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
  List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

  Page<Message> findByConversationId(Long conversationId, Pageable pageable);

  long countByConversationIdAndCreatedAtAfter(
    Long conversationId,
    java.time.LocalDateTime createdAt
  );

  // 只计算不是指定用户发送的消息（即别人发给当前用户的消息）
  long countByConversationIdAndCreatedAtAfterAndSenderIdNot(
    Long conversationId,
    java.time.LocalDateTime createdAt,
    Long senderId
  );
}
