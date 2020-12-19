package com.texastoc.model.season;

import com.texastoc.model.game.Game;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Season {

  private int id;
  private LocalDate start;
  private LocalDate end;

  // From TocConfig
  private int kittyPerGame;
  private int tocPerGame;
  private int quarterlyTocPerGame;
  private int quarterlyNumPayouts;
  private int buyInCost;
  private int rebuyAddOnCost;
  private int rebuyAddOnTocDebit;

  // Runtime variables. End with "Collected" for physical money in
  // money in for game buy-in
  private int buyInCollected;
  // money in for rebuy add on
  private int rebuyAddOnCollected;
  // money in for annual toc
  private int annualTocCollected;
  // all physical money collected which is buy-in, rebuy add on, annual toc
  private int totalCollected;

  // Runtime variables. End with "Calculated" for the where the money goes
  // rebuy add on that goes to annual TOC
  private int annualTocFromRebuyAddOnCalculated;
  // rebuy add on minus amount that goes to annual toc
  private int rebuyAddOnLessAnnualTocCalculated;
  // annual toc, annual toc from rebuy add on
  private int totalCombinedAnnualTocCalculated;
  // amount that goes to the kitty for supplies
  private int kittyCalculated;
  // total collected minus total combined toc collected minus kitty
  private int prizePotCalculated;

  // Other runtime variables
  private int numGames;
  private int numGamesPlayed;
  private LocalDateTime lastCalculated;
  private boolean finalized;

  private List<SeasonPlayer> players;
  private List<SeasonPayout> payouts;
  private List<SeasonPayout> estimatedPayouts;
  private List<QuarterlySeason> quarterlySeasons;
  private List<Game> games;

}
