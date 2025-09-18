package com.openisle.dto;

import java.util.List;
import lombok.Data;

/** Request body for saving a draft. */
@Data
public class DraftRequest {

  private String title;
  private String content;
  private Long categoryId;
  private List<Long> tagIds;
}
