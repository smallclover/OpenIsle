package com.openisle.mapper;

import com.openisle.dto.DraftDto;
import com.openisle.model.Draft;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Mapper for draft entities. */
@Component
public class DraftMapper {

  public DraftDto toDto(Draft draft) {
    DraftDto dto = new DraftDto();
    dto.setId(draft.getId());
    dto.setTitle(draft.getTitle());
    dto.setContent(draft.getContent());
    if (draft.getCategory() != null) {
      dto.setCategoryId(draft.getCategory().getId());
    }
    dto.setTagIds(
      draft
        .getTags()
        .stream()
        .map(tag -> tag.getId())
        .collect(Collectors.toList())
    );
    return dto;
  }
}
