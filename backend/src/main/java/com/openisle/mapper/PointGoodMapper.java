package com.openisle.mapper;

import com.openisle.dto.PointGoodDto;
import com.openisle.model.PointGood;
import org.springframework.stereotype.Component;

/** Mapper for point mall goods. */
@Component
public class PointGoodMapper {

  public PointGoodDto toDto(PointGood good) {
    PointGoodDto dto = new PointGoodDto();
    dto.setId(good.getId());
    dto.setName(good.getName());
    dto.setCost(good.getCost());
    dto.setImage(good.getImage());
    return dto;
  }
}
