package com.openisle.dto;

import lombok.Data;

/** Request to redeem a point mall good. */
@Data
public class PointRedeemRequest {

  private Long goodId;
  private String contact;
}
