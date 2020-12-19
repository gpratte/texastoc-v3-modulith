package com.texastoc.model.season;

import lombok.Data;

import java.util.List;

@Data
public class SeasonPayoutRange {

  private int lowRange;
  private int highRange;
  private List<SeasonPayoutPlace> guaranteed;
  private List<SeasonPayoutPlace> finalTable;

  @Data
  public static class SeasonPayoutPlace {
    private int place;
    private int amount;
    private int percent;
  }
}
