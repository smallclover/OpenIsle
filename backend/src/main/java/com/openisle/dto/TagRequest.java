package com.openisle.dto;

import lombok.Data;

/** Request body for creating or updating a tag. */
@Data
public class TagRequest {

  private String name;
  private String description;
  private String icon;
  private String smallIcon;
}
