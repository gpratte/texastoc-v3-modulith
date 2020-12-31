package com.texastoc.module.game.calculator;

import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePayout;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.repository.GameRepository;
import com.texastoc.module.settings.SettingsModuleFactory;
import com.texastoc.module.settings.model.Payout;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class PayoutCalculator {

  private final GameRepository gameRepository;

  public PayoutCalculator(GameRepository gameRepository) {
    this.gameRepository = gameRepository;
  }

  public Set<GamePayout> calculate(Game game) {
    if (game.getPrizePotCalculated() <= 0) {
      return Collections.emptySet();
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

  private LinkedHashSet<GamePayout> calculatePayout(int numToPay, Game game) {
    LinkedHashSet<GamePayout> gamePayouts = new LinkedHashSet<>(numToPay);

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

    List<Payout> payouts = SettingsModuleFactory.getSettingsModule().get().getPayouts().get(numToPay);

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

    // See if there is a chop
    List<Integer> chips = null;
    List<Integer> amounts = null;
    for (GamePlayer gamePlayer : game.getPlayers()) {
      if (gamePlayer.getChop() != null) {
        if (chips == null) {
          chips = new ArrayList<>();
          chips.add(gamePlayer.getChop());
          amounts = new ArrayList<>();
          for (GamePayout gamePayout : gamePayouts) {
            if (gamePayout.getPlace() == gamePlayer.getPlace()) {
              amounts.add(gamePayout.getAmount());
              break;
            }
          }
        } else {
          boolean inserted = false;
          for (int i = 0; i < chips.size(); ++i) {
            if (gamePlayer.getChop() >= chips.get(i)) {
              chips.add(i, gamePlayer.getChop());
              for (GamePayout gamePayout : gamePayouts) {
                if (gamePayout.getPlace() == gamePlayer.getPlace()) {
                  amounts.add(i, gamePayout.getAmount());
                  inserted = true;
                  break;
                }
              }
            }
          }
          if (!inserted) {
            chips.add(gamePlayer.getChop());
            for (GamePayout gamePayout : gamePayouts) {
              if (gamePayout != null && gamePayout.getPlace() == gamePlayer.getPlace()) {
                amounts.add(gamePayout.getAmount());
                break;
              }
            }
          }
        }
      }
    }

    if (chips != null) {
      // If chips are more than the number of payouts then there
      // is a problem because even though the top x chopped the
      // payouts less than that gets paid.
      int numChopThatGetPaid = Math.min(chips.size(), payouts.size());
      List<Chop> chops = ChopCalculator.calculate(
        chips.subList(0, numChopThatGetPaid),
        amounts.subList(0, numChopThatGetPaid));
      if (chops != null && chops.size() > 1) {
        for (Chop chop : chops) {
          outer:
          for (GamePlayer player : game.getPlayers()) {
            if (player.getChop() != null) {
              for (GamePayout gamePayout : gamePayouts) {
                if (gamePayout.getAmount() == chop.getOrgAmount()) {
                  gamePayout.setChopAmount(chop.getChopAmount());
                  gamePayout.setChopPercent(chop.getPercent());
                  break outer;
                }
              }
            }
          }
        }
      }
    }

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

  private void persistPayouts(LinkedHashSet<GamePayout> gamePayouts, int gameId) {
    Game game = gameRepository.findById(gameId).get();
    game.setPayouts(gamePayouts);
    gameRepository.save(game);
  }

}
