package com.texastoc.module.game.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.texastoc.module.season.model.Quarter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Game {
  // Read-only id set when game is created
  @Id
  private int id;

  // Read/write fields from API
  @Min(1)
  private int hostId;
  @Column("GAME_DATE")
  @NotNull
  private LocalDate date;
  private boolean transportRequired;

  // Read-only fields set by the server
  private String hostName;
  private int seasonId;
  private int qSeasonId;
  private Quarter quarter;
  private int seasonGameNum;
  private int quarterlyGameNum;

  // Game setup variables. End with "Cost" or "Debit"
  // Read-only fields set by the server
  private int kittyCost;
  private int buyInCost;
  private int rebuyAddOnCost;
  private int rebuyAddOnTocDebitCost;
  private int annualTocCost;
  private int quarterlyTocCost;

  // End with "Collected" for physical money collected
  // Read-only fields set by the server
  private int buyInCollected;
  // money in for rebuy add on
  private int rebuyAddOnCollected;
  // money in for annual toc
  private int annualTocCollected;
  // money in for quarterly toc
  private int quarterlyTocCollected;
  // all physical money collected which is buy-in, rebuy add on, annual toc, quarterly toc
  private int totalCollected;

  // End with "Calculated" for the where the money goes.
  // Read-only fields set by the server
  private int annualTocFromRebuyAddOnCalculated;
  // rebuy add on minus amount that goes to annual toc
  private int rebuyAddOnLessAnnualTocCalculated;
  // annual toc, quarterly toc, annual toc from rebuy add on
  private int totalCombinedTocCalculated;
  // amount that goes to the kitty for supplies
  private int kittyCalculated;
  // total collected minus total combined toc collected minus kitty
  private int prizePotCalculated;

  // Other game time variables
  // Read-only fields set by the server
  private int numPlayers;
  private int numPaidPlayers;
  private int payoutDelta;
  private boolean finalized;
  private boolean chopped;
  private boolean canRebuy = true;
  private LocalDateTime started;
  private LocalDateTime lastCalculated;

  @MappedCollection
  private List<GamePlayer> players;
  @MappedCollection
  private List<GamePayout> payouts;
  @MappedCollection(idColumn = "ID")
  private Seating seating;
}
