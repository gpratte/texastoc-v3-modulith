package com.texastoc.model.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GamePlayer implements Comparable<GamePlayer> {

  private int id;
  private int playerId;
  private int qSeasonId;
  private int seasonId;
  private int gameId;
  private String name;
  private Integer points;
  private Integer place;
  private Boolean knockedOut;
  private Boolean roundUpdates;
  private Integer buyInCollected;
  private Integer rebuyAddOnCollected;
  private Integer annualTocCollected;
  private Integer quarterlyTocCollected;
  private Integer chop;

  @Override
  public int compareTo(GamePlayer other) {
    // If I do not have a place and the other does then I come after
    if (place == null || place.intValue() > 10) {
      if (other.place != null && other.place <= 10) {
        return 1;
      }
    }

    // If I have a place
    if (place != null && place.intValue() <= 10) {
      // the other does not then I come before other
      if (other.place == null || other.place.intValue() > 10) {
        return -1;
      }
      // the other place is smaller than mine then I come after
      if (place.intValue() > other.place.intValue()) {
        return 1;
      }
      // If the place are equal then we are the same
      if (place.intValue() == other.place.intValue()) {
        return 0;
      }
      // the other place is larger than mine then I come before
      if (place.intValue() < other.place.intValue()) {
        return -1;
      }
    }

    // If I don't have a name
    if (name == null) {
      // then I come after other
      return 1;
    }

    // If other doesn't have a name
    if (other.name == null) {
      // then I come before other
      return -1;
    }

    return name.toLowerCase().compareTo(other.name.toLowerCase());
  }
}
