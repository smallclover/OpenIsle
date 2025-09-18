package com.openisle.dto;

import lombok.Data;

/** DTO representing a parent comment. */
@Data
public class ParentCommentDto {

  private Long id;
  private String author;
  private String content;
}
