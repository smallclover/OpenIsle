package com.openisle.dto;

import com.openisle.model.PointHistoryType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointHistoryDto {

  private Long id;
  private PointHistoryType type;
  private int amount;
  private int balance;
  private Long postId;
  private String postTitle;
  private Long commentId;
  private String commentContent;
  private Long fromUserId;
  private String fromUserName;
  private LocalDateTime createdAt;
}
