package com.texastoc.module.season.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuarterlySeason {
  private int id;
  private int seasonId;
  private LocalDate start;
  private LocalDate end;
  private boolean finalized;
  private Quarter quarter;
  private int numGames;
  private int numGamesPlayed;
  private int qTocCollected;
  private int qTocPerGame;
  private int numPayouts;
  private LocalDateTime lastCalculated;
  private List<QuarterlySeasonPlayer> players;
  private List<QuarterlySeasonPayout> payouts;
}
