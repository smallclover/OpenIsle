package com.openisle.dto;

import java.util.List;
import lombok.Data;

/** Request to mark notifications as read. */
@Data
public class NotificationMarkReadRequest {

  private List<Long> ids;
}
