package com.openisle.mapper;

import com.openisle.dto.CategoryDto;
import com.openisle.model.Category;
import org.springframework.stereotype.Component;

/** Mapper for category entities. */
@Component
public class CategoryMapper {

  public CategoryDto toDto(Category c) {
    return toDto(c, null);
  }

  public CategoryDto toDto(Category c, Long count) {
    CategoryDto dto = new CategoryDto();
    dto.setId(c.getId());
    dto.setName(c.getName());
    dto.setDescription(c.getDescription());
    dto.setIcon(c.getIcon());
    dto.setSmallIcon(c.getSmallIcon());
    dto.setCount(count);
    return dto;
  }
}
