package com.openisle.dto;

import lombok.Data;

/** Request body for saving a push subscription. */
@Data
public class PushSubscriptionRequest {

  private String endpoint;
  private String p256dh;
  private String auth;
}
