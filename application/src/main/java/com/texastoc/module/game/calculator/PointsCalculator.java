package com.texastoc.module.game.calculator;

import com.texastoc.module.game.calculator.icm.ICMCalculator;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.repository.GameRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class PointsCalculator {

  private final Double tenthPlaceIncr;
  private final Integer tenthPlacePoints;
  private final Double multiplier;
  private final GameRepository gameRepository;

  private final Map<Integer, Map<Integer, Integer>> POINT_SYSTEM = new HashMap<>();

  public PointsCalculator(@Value("${points.tenthPlaceIncr}") Double tenthPlaceIncr, @Value("${points.tenthPlacePoints}") Integer tenthPlacePoints, @Value("${points.multiplier}") Double multiplier, GameRepository gameRepository) {
    this.tenthPlaceIncr = tenthPlaceIncr;
    this.tenthPlacePoints = tenthPlacePoints;
    this.multiplier = multiplier;
    this.gameRepository = gameRepository;
  }

  public void calculate(Game game) {
    boolean calculationRequired = false;
    for (GamePlayer gamePlayer : game.getPlayers()) {
      if (gamePlayer.getPlace() != null && gamePlayer.getPlace() < 11) {
        calculationRequired = true;
      }
    }

    if (!calculationRequired) {
      return;
    }

    // Get the points for a game with given number of players
    Map<Integer, Integer> placePoints = calculatePlacePoints(game.getNumPlayers());

    // Apply the chop
    Map<Integer, Integer> placeChopPoints = chopPoints(game.getPlayers(), placePoints);

    // Apply the points to players that participate
    for (GamePlayer gamePlayer : game.getPlayers()) {
      if (gamePlayer.getPlace() != null && gamePlayer.getPlace() < 11) {
        if (gamePlayer.isAnnualTocParticipant()) {
          gamePlayer.setTocPoints(placePoints.get(gamePlayer.getPlace()));
          if (placeChopPoints != null && placeChopPoints.get(gamePlayer.getPlace()) != null) {
            gamePlayer.setTocChopPoints(placeChopPoints.get(gamePlayer.getPlace()));
          }
        }
        if (gamePlayer.isQuarterlyTocParticipant()) {
          gamePlayer.setQTocPoints(placePoints.get(gamePlayer.getPlace()));
          if (placeChopPoints != null && placeChopPoints.get(gamePlayer.getPlace()) != null) {
            gamePlayer.setQTocChopPoints(placeChopPoints.get(gamePlayer.getPlace()));
          }
        }
      }
    }

    gameRepository.save(game);
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

  private Map<Integer, Integer> chopPoints(List<GamePlayer> gamePlayers, Map<Integer, Integer> placePoints) {
    List<Integer> chips = new LinkedList<>();
    outer:
    for (int i = 1; i <= 10; i++) {
      for (GamePlayer gamePlayer : gamePlayers) {
        if (gamePlayer.getPlace() != null && gamePlayer.getPlace() == i) {
          if (gamePlayer.getChop() == null) {
            break outer;
          }
          chips.add(gamePlayer.getChop());
        }
      }
    }

    if (chips.size() == 0) {
      return null;
    }

    int sumOriginal = 0;
    List<Integer> originalPoints = new ArrayList<>(chips.size());
    for (int i = 0; i < chips.size(); i++) {
      Integer original = placePoints.get(i + 1);
      originalPoints.add(original);
      sumOriginal += original;
    }
    List<Double> chopPointsWithDecmials = ICMCalculator.calculate(originalPoints, chips);

    // Round the chopped amounts
    List<Integer> chopAmountsRounded = new ArrayList<>(chips.size());
    for (Double chopAmountsWithDecmial : chopPointsWithDecmials) {
      int chopAmountRounded = (int) Math.round(chopAmountsWithDecmial);
      chopAmountsRounded.add(chopAmountRounded);
    }

    // Make sure the sum of the rounded amounts is the same as the sum of the original amounts
    ChopUtils.adjustTotal(sumOriginal, chopAmountsRounded);

    Map<Integer, Integer> chopPoints = new HashMap<>();
    for (int i = 0; i < chips.size(); i++) {
      chopPoints.put(i + 1, chopAmountsRounded.get(i));
    }
    return chopPoints;
  }

//  static Comparator<GamePlayer> sortByChop = new Comparator<GamePlayer>() {
//    @Override
//    public int compare(GamePlayer o1, GamePlayer o2) {
//      if (o1.getChop().intValue() == o2.getChop().intValue()) {
//        return 0;
//      }
//      return o1.getChop() - o2.getChop();
//    }
//  };
}
