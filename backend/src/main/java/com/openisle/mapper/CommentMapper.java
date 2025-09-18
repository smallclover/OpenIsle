package com.openisle.mapper;

import com.openisle.dto.CommentDto;
import com.openisle.model.Comment;
import com.openisle.service.CommentService;
import com.openisle.service.ReactionService;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Mapper for comments including replies and reactions. */
@Component
@RequiredArgsConstructor
public class CommentMapper {

  private final CommentService commentService;
  private final ReactionService reactionService;
  private final ReactionMapper reactionMapper;
  private final UserMapper userMapper;

  public CommentDto toDto(Comment comment) {
    CommentDto dto = new CommentDto();
    dto.setId(comment.getId());
    dto.setContent(comment.getContent());
    dto.setCreatedAt(comment.getCreatedAt());
    dto.setPinnedAt(comment.getPinnedAt());
    dto.setAuthor(userMapper.toAuthorDto(comment.getAuthor()));
    dto.setReward(0);
    return dto;
  }

  public CommentDto toDtoWithReplies(Comment comment) {
    CommentDto dto = toDto(comment);
    dto.setReplies(
      commentService
        .getReplies(comment.getId())
        .stream()
        .map(this::toDtoWithReplies)
        .collect(Collectors.toList())
    );
    dto.setReactions(
      reactionService
        .getReactionsForComment(comment.getId())
        .stream()
        .map(reactionMapper::toDto)
        .collect(Collectors.toList())
    );
    return dto;
  }
}
