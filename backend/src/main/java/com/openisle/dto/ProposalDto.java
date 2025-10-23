package com.openisle.dto;

import com.openisle.model.CategoryProposalStatus;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProposalDto extends PollDto {

  private CategoryProposalStatus proposalStatus;
  private String proposedName;
  private String description;
  private int approveThreshold;
  private int quorum;
  private LocalDateTime startAt;
  private String resultSnapshot;
  private String rejectReason;
}
