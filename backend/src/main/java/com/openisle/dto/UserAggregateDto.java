package com.openisle.dto;

import java.util.List;
import lombok.Data;

/** Aggregated user data including posts and replies. */
@Data
public class UserAggregateDto {

  private UserDto user;
  private List<PostMetaDto> posts;
  private List<CommentInfoDto> replies;
}
