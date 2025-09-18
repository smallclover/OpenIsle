package com.openisle.mapper;

import com.openisle.dto.PointHistoryDto;
import com.openisle.model.PointHistory;
import org.springframework.stereotype.Component;

@Component
public class PointHistoryMapper {

  public PointHistoryDto toDto(PointHistory history) {
    PointHistoryDto dto = new PointHistoryDto();
    dto.setId(history.getId());
    dto.setType(history.getType());
    dto.setAmount(history.getAmount());
    dto.setBalance(history.getBalance());
    dto.setCreatedAt(history.getCreatedAt());
    if (history.getPost() != null) {
      dto.setPostId(history.getPost().getId());
      dto.setPostTitle(history.getPost().getTitle());
    }
    if (history.getComment() != null) {
      dto.setCommentId(history.getComment().getId());
      dto.setCommentContent(history.getComment().getContent());
      if (history.getComment().getPost() != null && dto.getPostId() == null) {
        dto.setPostId(history.getComment().getPost().getId());
        dto.setPostTitle(history.getComment().getPost().getTitle());
      }
    }
    if (history.getFromUser() != null) {
      dto.setFromUserId(history.getFromUser().getId());
      dto.setFromUserName(history.getFromUser().getUsername());
    }
    return dto;
  }
}
