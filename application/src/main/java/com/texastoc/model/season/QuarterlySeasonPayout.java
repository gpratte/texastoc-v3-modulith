package com.texastoc.model.season;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuarterlySeasonPayout {

  private int id;
  private int seasonId;
  private int qSeasonId;
  private int place;
  private int amount;
}
