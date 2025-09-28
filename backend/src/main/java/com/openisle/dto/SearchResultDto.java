package com.openisle.dto;

import lombok.Data;

/** DTO representing a search result entry. */
@Data
public class SearchResultDto {

  private String type;
  private Long id;
  private String text;
  private String subText;
  private String extra;
  private Long postId;
  private String highlightedText;
  private String highlightedSubText;
  private String highlightedExtra;
}
