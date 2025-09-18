package com.openisle.dto;

import lombok.Data;

/**
 * DTO representing a post category.
 */
@Data
public class CategoryDto {

  private Long id;
  private String name;
  private String description;
  private String icon;
  private String smallIcon;
  private Long count;
}
