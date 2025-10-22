package com.openisle.mapper;

import com.openisle.dto.CategoryDto;
import com.openisle.dto.PostChangeLogDto;
import com.openisle.dto.TagDto;
import com.openisle.model.*;
import com.openisle.repository.CategoryRepository;
import com.openisle.repository.TagRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostChangeLogMapper {

  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;
  private final CategoryMapper categoryMapper;
  private final TagMapper tagMapper;

  public PostChangeLogDto toDto(PostChangeLog log) {
    PostChangeLogDto dto = new PostChangeLogDto();
    dto.setId(log.getId());
    if (log.getUser() != null) {
      dto.setUsername(log.getUser().getUsername());
      dto.setUserAvatar(log.getUser().getAvatar());
    }
    dto.setType(log.getType());
    dto.setTime(log.getCreatedAt());
    if (log instanceof PostTitleChangeLog t) {
      dto.setOldTitle(t.getOldTitle());
      dto.setNewTitle(t.getNewTitle());
    } else if (log instanceof PostContentChangeLog c) {
      dto.setOldContent(c.getOldContent());
      dto.setNewContent(c.getNewContent());
    } else if (log instanceof PostCategoryChangeLog cat) {
      dto.setOldCategory(mapCategory(cat.getOldCategory()));
      dto.setNewCategory(mapCategory(cat.getNewCategory()));
    } else if (log instanceof PostTagChangeLog tag) {
      dto.setOldTags(mapTags(tag.getOldTags()));
      dto.setNewTags(mapTags(tag.getNewTags()));
    } else if (log instanceof PostClosedChangeLog cl) {
      dto.setOldClosed(cl.isOldClosed());
      dto.setNewClosed(cl.isNewClosed());
    } else if (log instanceof PostPinnedChangeLog p) {
      dto.setOldPinnedAt(p.getOldPinnedAt());
      dto.setNewPinnedAt(p.getNewPinnedAt());
    } else if (log instanceof PostFeaturedChangeLog f) {
      dto.setOldFeatured(f.isOldFeatured());
      dto.setNewFeatured(f.isNewFeatured());
    } else if (log instanceof PostDonateChangeLog d) {
      dto.setAmount(d.getAmount());
    }
    return dto;
  }

  private CategoryDto mapCategory(String name) {
    if (name == null) {
      return null;
    }
    return categoryRepository
      .findByName(name)
      .map(categoryMapper::toDto)
      .orElseGet(() -> {
        CategoryDto dto = new CategoryDto();
        dto.setName(name);
        return dto;
      });
  }

  private List<TagDto> mapTags(String tags) {
    if (tags == null || tags.isBlank()) {
      return Collections.emptyList();
    }
    return Arrays.stream(tags.split(","))
      .map(String::trim)
      .map(this::mapTag)
      .collect(Collectors.toList());
  }

  private TagDto mapTag(String name) {
    return tagRepository
      .findByName(name)
      .map(tagMapper::toDto)
      .orElseGet(() -> {
        TagDto dto = new TagDto();
        dto.setName(name);
        return dto;
      });
  }
}
