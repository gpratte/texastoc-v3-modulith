package com.texastoc.model.game;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GamePayout {

  private int id;
  private int gameId;
  private int place;
  private int amount;
  private Integer chopAmount;
  private Double chopPercent;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GamePayout that = (GamePayout) o;
    return gameId == that.gameId &&
      place == that.place &&
      amount == that.amount &&
      Objects.equals(chopAmount, that.chopAmount) &&
      Objects.equals(chopPercent, that.chopPercent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gameId, place, amount, chopAmount, chopPercent);
  }
}
