package com.texastoc.model.game;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.texastoc.model.season.Quarter;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class Game {

  private int id;
  private int seasonId;
  private int qSeasonId;

  private int hostId;

  private LocalDate date;

  // Denormalized fields
  private String hostName;
  private Quarter quarter;

  // Game setup variables. End with "Cost" or "Debit"
  private boolean transportRequired;
  private int kittyCost;
  private int buyInCost;
  private int rebuyAddOnCost;
  private int rebuyAddOnTocDebit;
  private int annualTocCost;
  private int quarterlyTocCost;

  // Game time variables. End with "Collected" for physical money in
  // money in for game buy-in
  private int buyInCollected;
  // money in for rebuy add on
  private int rebuyAddOnCollected;
  // money in for annual toc
  private int annualTocCollected;
  // money in for quarterly toc
  private int quarterlyTocCollected;
  // all physical money collected which is buy-in, rebuy add on, annual toc, quarterly toc
  private int totalCollected;

  // Game time variables. End with "Calculated" for the where the money goes
  // rebuy add on that goes to annual TOC
  private int annualTocFromRebuyAddOnCalculated;
  // rebuy add on minus amount that goes to annual toc
  private int rebuyAddOnLessAnnualTocCalculated;
  // annual toc, quarterly toc, annual toc from rebuy add on
  private int totalCombinedTocCalculated;
  // amount that goes to the kitty for supplies
  private int kittyCalculated;
  // total collected minus total combined toc collected minus kitty
  private int prizePotCalculated;
  // TODO
  private int averageStack;
  private int numPaidPlayers;
  private int numPaidPlayersRemaining;
  private int seasonGameNum;
  private int quarterlyGameNum;

  // Other game time variables
  private int payoutDelta;
  private boolean finalized;
  private LocalDateTime started;
  private int numPlayers;
  private LocalDateTime lastCalculated;
  private boolean canRebuy = true;

  private List<GamePlayer> players;
  private List<GamePayout> payouts;

  private Seating seating;
}
