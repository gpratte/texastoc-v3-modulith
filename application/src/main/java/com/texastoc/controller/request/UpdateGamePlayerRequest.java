package com.texastoc.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateGamePlayerRequest {
  private Integer place;
  private boolean knockedOut;
  private boolean roundUpdates;
  private boolean buyInCollected;
  private boolean rebuyAddOnCollected;
  private boolean annualTocCollected;
  private boolean quarterlyTocCollected;
  private Integer chop;
}
