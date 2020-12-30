package com.texastoc.module.game.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

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
  private String firstName;
  private String lastName;
  private String email;
  private Integer points;
  private Integer place;
  private Boolean knockedOut;
  private Boolean roundUpdates;
  private Integer buyInCollected;
  private Integer rebuyAddOnCollected;
  private Integer annualTocCollected;
  private Integer quarterlyTocCollected;
  private Integer chop;

  public String getName() {
    String name = null;

    if (firstName != null) {
      name = firstName;
      if (lastName != null) {
        name += " " + lastName;
      }
    } else if (lastName != null) {
      name = lastName;
    }

    return name == null ? "Unknown" : name;
  }

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

    // If I don't have a first or a last
    if (firstName == null && lastName == null) {
      // then I come after other
      return 1;
    }

    // If other doesn't have a first or a last
    if (other.firstName == null && other.lastName == null) {
      // then I come before other
      return -1;
    }

    return makeFullName(this).compareTo(makeFullName(other));
  }

  private String makeFullName(GamePlayer player) {
    // Combine the first and last into a full name
    StringBuffer fullName = new StringBuffer();
    if (!StringUtils.isBlank(player.firstName)) {
      fullName.append(player.firstName);
    }
    if (!StringUtils.isBlank(player.firstName) && !StringUtils.isBlank(player.lastName)) {
      fullName.append(" ");
    }
    if (!StringUtils.isBlank(player.lastName)) {
      fullName.append(player.lastName);
    }
    return fullName.toString().toLowerCase();
  }
}
