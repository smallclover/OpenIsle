package com.openisle.dto;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Detailed DTO for a post, including comments.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostDetailDto extends PostSummaryDto {

  private List<CommentDto> comments;
}
