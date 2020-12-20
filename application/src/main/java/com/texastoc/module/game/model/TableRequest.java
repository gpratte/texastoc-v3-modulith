package com.texastoc.module.game.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableRequest {

  private int gamePlayerId;
  private String playerName;
  private int tableNum;
}