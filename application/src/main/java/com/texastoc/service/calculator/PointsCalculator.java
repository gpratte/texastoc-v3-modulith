package com.texastoc.service.calculator;

import com.texastoc.model.game.Game;
import com.texastoc.model.game.GamePlayer;
import com.texastoc.repository.GamePlayerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PointsCalculator {

  private final Double tenthPlaceIncr;
  private final Integer tenthPlacePoints;
  private final Double multiplier;
  private final GamePlayerRepository gamePlayerRepository;

  private final Map<Integer, Map<Integer, Integer>> POINT_SYSTEM = new HashMap<>();

  public PointsCalculator(@Value("${points.tenthPlaceIncr}") Double tenthPlaceIncr, @Value("${points.tenthPlacePoints}") Integer tenthPlacePoints, @Value("${points.multiplier}") Double multiplier, GamePlayerRepository gamePlayerRepository) {
    this.tenthPlaceIncr = tenthPlaceIncr;
    this.tenthPlacePoints = tenthPlacePoints;
    this.multiplier = multiplier;
    this.gamePlayerRepository = gamePlayerRepository;
  }

  public List<GamePlayer> calculate(Game game, List<GamePlayer> gamePlayers) {

    boolean calculationRequired = false;
    for (GamePlayer gamePlayer : gamePlayers) {
      if (gamePlayer.getPlace() != null && gamePlayer.getPlace() < 11) {
        calculationRequired = true;
        break;
      }
    }
    if (!calculationRequired) {
      return gamePlayers;
    }

    // Get the points for a game with given number of players
    Map<Integer, Integer> placePoints = calculatePlacePoints(game.getNumPlayers());

    boolean pointsChanged = false;
    for (GamePlayer gamePlayer : gamePlayers) {
      // Check if game player is in top 10
      if (gamePlayer.getPlace() != null && gamePlayer.getPlace() < 11) {
        if (gamePlayer.getPoints() == null || gamePlayer.getPoints().intValue() != placePoints.get(gamePlayer.getPlace())) {
          pointsChanged = true;
          gamePlayer.setPoints(placePoints.get(gamePlayer.getPlace()));
        }
      } else if (gamePlayer.getPoints() != null) {
        pointsChanged = true;
        gamePlayer.setPoints(null);
      }
    }

    // See if there is a chop
    boolean chopRequired = false;
    for (GamePlayer gamePlayer : gamePlayers) {
      if (gamePlayer.getChop() != null) {
        chopRequired = true;
        break;
      }
    }
    if (!chopRequired) {
      if (pointsChanged) {
        persistPoints(gamePlayers);
      }
      return gamePlayers;
    }


    List<Integer> chips = null;
    List<Integer> amounts = null;
    for (GamePlayer gamePlayer : gamePlayers) {
      if (gamePlayer.getChop() != null) {
        if (chips == null) {
          chips = new ArrayList<>();
          chips.add(gamePlayer.getChop());
          amounts = new ArrayList<>();
          if (gamePlayer.getPoints() != null) {
            amounts.add(gamePlayer.getPoints());
          }
        } else {
          boolean inserted = false;
          for (int i = 0; i < chips.size(); ++i) {
            if (gamePlayer.getChop() >= chips.get(i)) {
              chips.add(i, gamePlayer.getChop());
              if (gamePlayer.getPoints() != null) {
                amounts.add(i, gamePlayer.getPoints());
              }
              inserted = true;
              break;
            }
          }
          if (!inserted) {
            chips.add(gamePlayer.getChop());
            if (gamePlayer.getPoints() != null) {
              amounts.add(gamePlayer.getPoints());
            }
          }
        }
      }
    }

    if (chips != null) {
      List<Chop> chops = ChopCalculator.calculate(chips, amounts);
      if (chops != null && chops.size() > 1) {
        for (Chop chop : chops) {
          for (GamePlayer gamePlayer : gamePlayers) {
            if (gamePlayer.getPoints() != null &&
              gamePlayer.getPoints() == chop.getOrgAmount()) {
              gamePlayer.setPoints(chop.getChopAmount());
              break;
            }
          }
        }
      }
      persistPoints(gamePlayers);
    }

    return gamePlayers;
  }

  /**
   * If the place/points Set is in the cache for the number of players return it.
   * <p>
   * Otherwise calculate the Set of place/points for the number of players, add it to the cache and return it.
   */
  public Map<Integer, Integer> calculatePlacePoints(int numPlayers) {
    if (POINT_SYSTEM.get(numPlayers) != null) {
      return POINT_SYSTEM.get(numPlayers);
    }

    Map<Integer, Integer> placePoints = new HashMap<>();

    double value = tenthPlacePoints;

    for (int i = 2; i < numPlayers; ++i) {
      value += tenthPlaceIncr;
    }

    int players = Math.min(numPlayers, 10);

    if (players == 10) {
      placePoints.put(10, Long.valueOf(Math.round(value)).intValue());
    } else {
      placePoints.put(10, 0);
    }

    for (int i = 9; i > 0; --i) {
      value *= multiplier;
      if (players >= i) {
        placePoints.put(i, Long.valueOf(Math.round(value))
          .intValue());
      } else {
        placePoints.put(i, 0);
      }
    }

    POINT_SYSTEM.put(numPlayers, placePoints);
    return placePoints;
  }

  private void persistPoints(List<GamePlayer> gamePlayers) {
    for (GamePlayer gamePlayer : gamePlayers) {
      GamePlayer currentGamePlayer = gamePlayerRepository.selectById(gamePlayer.getId());

      boolean isToc = currentGamePlayer.getAnnualTocCollected() != null && currentGamePlayer.getAnnualTocCollected() > 0;
      boolean isQToc = currentGamePlayer.getQuarterlyTocCollected() != null && currentGamePlayer.getQuarterlyTocCollected() > 0;

      if (isToc || isQToc) {
        currentGamePlayer.setPoints(gamePlayer.getPoints());
      } else {
        currentGamePlayer.setPoints(null);
      }
      gamePlayerRepository.update(currentGamePlayer);
    }
  }

}
