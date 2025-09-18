package com.openisle.dto;

import com.openisle.model.PostChangeType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostChangeLogDto {

  private Long id;
  private String username;
  private String userAvatar;
  private PostChangeType type;
  private LocalDateTime time;
  private String oldTitle;
  private String newTitle;
  private String oldContent;
  private String newContent;
  private CategoryDto oldCategory;
  private CategoryDto newCategory;
  private List<TagDto> oldTags;
  private List<TagDto> newTags;
  private Boolean oldClosed;
  private Boolean newClosed;
  private LocalDateTime oldPinnedAt;
  private LocalDateTime newPinnedAt;
  private Boolean oldFeatured;
  private Boolean newFeatured;
}
