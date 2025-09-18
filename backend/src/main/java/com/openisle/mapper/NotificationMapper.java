package com.openisle.mapper;

import com.openisle.dto.NotificationDto;
import com.openisle.dto.PostSummaryDto;
import com.openisle.model.Comment;
import com.openisle.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Mapper for notifications. */
@Component
@RequiredArgsConstructor
public class NotificationMapper {

  private final CommentMapper commentMapper;
  private final UserMapper userMapper;

  public NotificationDto toDto(Notification n) {
    NotificationDto dto = new NotificationDto();
    dto.setId(n.getId());
    dto.setType(n.getType());
    if (n.getPost() != null) {
      PostSummaryDto postDto = new PostSummaryDto();
      postDto.setId(n.getPost().getId());
      postDto.setTitle(n.getPost().getTitle());
      dto.setPost(postDto);
    }
    if (n.getComment() != null) {
      dto.setComment(commentMapper.toDto(n.getComment()));
      Comment parent = n.getComment().getParent();
      if (parent != null) {
        dto.setParentComment(commentMapper.toDto(parent));
      }
    }
    if (n.getFromUser() != null) {
      dto.setFromUser(userMapper.toAuthorDto(n.getFromUser()));
    }
    if (n.getReactionType() != null) {
      dto.setReactionType(n.getReactionType());
    }
    dto.setApproved(n.getApproved());
    dto.setContent(n.getContent());
    dto.setRead(n.isRead());
    dto.setCreatedAt(n.getCreatedAt());
    return dto;
  }
}
