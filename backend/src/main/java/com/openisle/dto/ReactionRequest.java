package com.openisle.dto;

import com.openisle.model.ReactionType;
import lombok.Data;

/** Request for reacting to a post or comment. */
@Data
public class ReactionRequest {

  private ReactionType type;
}
