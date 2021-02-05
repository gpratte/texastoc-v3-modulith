package com.texastoc.module.game.calculator;

import com.texastoc.module.game.calculator.icm.ICMCalculator;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePayout;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.repository.GameRepository;
import com.texastoc.module.settings.SettingsModule;
import com.texastoc.module.settings.SettingsModuleFactory;
import com.texastoc.module.settings.model.Payout;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Component
public class PayoutCalculator {

  private final GameRepository gameRepository;
  private SettingsModule settingsModule;

  public PayoutCalculator(GameRepository gameRepository) {
    this.gameRepository = gameRepository;
  }

  public List<GamePayout> calculate(Game game) {
    if (game.getPrizePotCalculated() <= 0) {
      return Collections.emptyList();
    }

    // round to multiple of 5 (e.g. 12 rounds to 10 but 13 rounds to 15)
    int numPlayers = (int) Math.round((double) game.getNumPlayers() / 5) * 5;

    int numberPaid = numPlayers / 5;
    numberPaid += game.getPayoutDelta();

    // Always pay at least 1 player
    if (numberPaid < 1) {
      numberPaid = 1;
    }
    // Cap at 10
    if (numberPaid > 10) {
      numberPaid = 10;
    }
    return calculatePayout(numberPaid, game);
  }

  private List<GamePayout> calculatePayout(int numToPay, Game game) {
    List<GamePayout> gamePayouts = new ArrayList<>(numToPay);

    // If only one player then he gets it all
    if (numToPay == 1) {
      gamePayouts.add(GamePayout.builder()
        .gameId(game.getId())
        .place(1)
        .amount(game.getPrizePotCalculated())
        .build());
      persistPayouts(gamePayouts, game.getId());
      return gamePayouts;
    }

    List<Payout> payouts = getSettingsModule().get().getPayouts().get(numToPay);

    int prizePot = game.getPrizePotCalculated();
    int totalPayout = 0;
    for (Payout payout : payouts) {
      GamePayout gp = new GamePayout();
      gp.setGameId(game.getId());
      gp.setPlace(payout.getPlace());
      double percent = payout.getPercent();
      int amount = (int) Math.round(percent * prizePot);
      gp.setAmount(amount);
      totalPayout += amount;
      gamePayouts.add(gp);
    }

    // Adjust if payouts are more or less than prize pot
    adjustPayouts(totalPayout, prizePot, gamePayouts);

    // See if there is a chop
    chopPayouts(game.getPlayers(), gamePayouts);

    // TODO check if the game payouts are not the same as the current payouts
    // flag if the payouts changed
//    boolean payoutsChanged = false;
//    Set<GamePayout> currentPayouts = gameRepository.findById(game.getId()).get().getPayouts();
//    if (gamePayouts.size() != currentPayouts.size()) {
//      payoutsChanged = true;
//    } else {
//       figure this out
//    }
//
//    if (payoutsChanged) {
//      persistPayouts(gamePayouts, game.getId());
//    }
    persistPayouts(gamePayouts, game.getId());

    return gamePayouts;
  }

  private void adjustPayouts(int totalPayout, int prizePot, List<GamePayout> gamePayouts) {
    if (totalPayout > prizePot) {
      int extra = totalPayout - prizePot;
      while (extra > 0) {
        for (int i = gamePayouts.size() - 1; i >= 0; --i) {
          for (GamePayout gp : gamePayouts) {
            if (gp.getPlace() == i) {
              gp.setAmount(gp.getAmount() - 1);
              if (--extra == 0) {
                break;
              }
            }
          }
        }
      }
    } else if (totalPayout < prizePot) {
      int extra = prizePot - totalPayout;
      while (extra > 0) {
        for (GamePayout gp : gamePayouts) {
          gp.setAmount(gp.getAmount() + 1);
          if (--extra == 0) {
            break;
          }
        }
      }
    }
  }

  private void chopPayouts(List<GamePlayer> gamePlayers, List<GamePayout> gamePayouts) {
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
      return;
    }

    int sumOriginal = 0;
    List<Integer> originalPayoutAmounts = new ArrayList<>(chips.size());
    for (int i = 0; i < chips.size(); i++) {
      GamePayout gamePayout = gamePayouts.get(i);
      originalPayoutAmounts.add(gamePayout.getAmount());
      sumOriginal += gamePayout.getAmount();
    }
    List<Double> chopAmountsWithDecmials = ICMCalculator.calculate(originalPayoutAmounts, chips);

    // Round the chopped amounts
    List<Integer> chopAmountsRounded = new ArrayList<>(chips.size());
    for (Double chopAmountsWithDecmial : chopAmountsWithDecmials) {
      int chopAmountRounded = (int) Math.round(chopAmountsWithDecmial);
      chopAmountsRounded.add(chopAmountRounded);
    }

    // Make sure the sum of the rounded amounts is the same as the sum of the original amounts
    ChopUtils.adjustTotal(sumOriginal, chopAmountsRounded);

    for (int i = 0; i < chips.size(); i++) {
      gamePayouts.get(i).setChopAmount(chopAmountsRounded.get(i));
    }
  }

  private void persistPayouts(List<GamePayout> gamePayouts, int gameId) {
    Game game = gameRepository.findById(gameId).get();
    game.setPayouts(gamePayouts);
    gameRepository.save(game);
  }

  private SettingsModule getSettingsModule() {
    if (settingsModule == null) {
      settingsModule = SettingsModuleFactory.getSettingsModule();
    }
    return settingsModule;
  }

}
