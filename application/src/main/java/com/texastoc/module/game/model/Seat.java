package com.texastoc.module.game.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
  private int id;
  private int seatNum;
  private int tableNum;
  private Integer gamePlayerId;
  private String gamePlayerName;
}
