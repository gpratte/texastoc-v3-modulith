package com.texastoc.model.game.clock;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Round {
  private String name;
  private int smallBlind;
  private int bigBlind;
  private int ante;
  private int duration;
}
