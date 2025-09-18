package com.openisle.dto;

import lombok.Data;

/** Request body for creating or updating a category. */
@Data
public class CategoryRequest {

  private String name;
  private String description;
  private String icon;
  private String smallIcon;
}
