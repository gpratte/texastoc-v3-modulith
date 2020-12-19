package com.texastoc.model.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

  private int gameId;
  private int seatNumber;
  private int tableNumber;
  private Integer gamePlayerId;
  private String gamePlayerName;
}
