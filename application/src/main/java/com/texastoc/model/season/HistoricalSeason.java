package com.texastoc.model.season;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalSeason {
  private int id;
  private int seasonId;
  private String startYear;
  private String endYear;
  private List<HistoricalSeasonPlayer> players;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class HistoricalSeasonPlayer {
    private String firstName;
    private String lastName;
    private String name;
    private int points;
    private int entries;
  }
}
