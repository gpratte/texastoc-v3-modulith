package com.texastoc.module.game.model;

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
// TODO remove
public class FirstTimeGamePlayer {
  private String firstName;
  private String lastName;
  private String email;
  private boolean buyInCollected;
  private boolean annualTocCollected;
  private boolean quarterlyTocCollected;
}
