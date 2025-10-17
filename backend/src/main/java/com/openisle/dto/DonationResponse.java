package com.openisle.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonationResponse {

  private int totalAmount;
  private List<DonationDto> donations = new ArrayList<>();
  private Integer balance;
}
