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
public class CreateGamePlayerRequest {
  private int playerId;
  private boolean buyInCollected;
  private boolean annualTocCollected;
  private boolean quarterlyTocCollected;
}
