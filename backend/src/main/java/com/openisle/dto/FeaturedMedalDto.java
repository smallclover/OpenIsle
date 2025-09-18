package com.openisle.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FeaturedMedalDto extends MedalDto {

  private long currentFeaturedCount;
  private long targetFeaturedCount;
}
