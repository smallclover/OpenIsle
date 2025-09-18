package com.openisle.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageNotificationPayload implements Serializable {

  private String targetUsername;
  private Object payload;
}
