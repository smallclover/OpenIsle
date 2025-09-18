package com.openisle.mapper;

import com.openisle.dto.TagDto;
import com.openisle.model.Tag;
import org.springframework.stereotype.Component;

/** Mapper for tag entities. */
@Component
public class TagMapper {

  public TagDto toDto(Tag tag) {
    return toDto(tag, null);
  }

  public TagDto toDto(Tag tag, Long count) {
    TagDto dto = new TagDto();
    dto.setId(tag.getId());
    dto.setName(tag.getName());
    dto.setDescription(tag.getDescription());
    dto.setIcon(tag.getIcon());
    dto.setSmallIcon(tag.getSmallIcon());
    dto.setCreatedAt(tag.getCreatedAt());
    dto.setCount(count);
    return dto;
  }
}
