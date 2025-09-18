package com.openisle.dto;

import java.util.List;
import lombok.Data;

/** DTO representing a saved draft. */
@Data
public class DraftDto {

  private Long id;
  private String title;
  private String content;
  private Long categoryId;
  private List<Long> tagIds;
}
