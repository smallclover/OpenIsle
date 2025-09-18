package com.openisle.dto;

import lombok.Data;

/** Request body for creating or replying to a comment. */
@Data
public class CommentRequest {

  private String content;
  private String captcha;
}
