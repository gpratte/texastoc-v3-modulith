package com.texastoc.module.game.calculator;

import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.repository.GameRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class GameCalculator {

  private final GameRepository gameRepository;

  public GameCalculator(GameRepository gameRepository) {
    this.gameRepository = gameRepository;
  }

  public Game calculate(Game game) {
    int numPlayers = 0;
    int buyInCollected = 0;
    int rebuyAddOnCollected = 0;
    int annualTocCollected = 0;
    int quarterlyTocCollected = 0;
    int totalCollected = 0;
    int kittyCalculated = 0;
    int annualTocFromRebuyAddOnCalculated = 0;

    if (game.getPlayers() != null) {
      for (GamePlayer gamePlayer : game.getPlayers()) {
        ++numPlayers;
        buyInCollected += gamePlayer.isBoughtIn() ? game.getBuyInCost() : 0;
        rebuyAddOnCollected += gamePlayer.isRebought() ? game.getRebuyAddOnCost() : 0;
        annualTocCollected += gamePlayer.isAnnualTocParticipant() ? game.getAnnualTocCost() : 0;
        quarterlyTocCollected += gamePlayer.isQuarterlyTocParticipant() ? game.getQuarterlyTocCost() : 0;

        if (gamePlayer.isAnnualTocParticipant() && gamePlayer.isRebought()) {
          annualTocFromRebuyAddOnCalculated += game.getRebuyAddOnTocDebitCost();
        }
      }
    }

    if (buyInCollected > 0) {
      kittyCalculated = game.getKittyCost();
    }

    game.setNumPlayers(numPlayers);
    game.setBuyInCollected(buyInCollected);
    game.setRebuyAddOnCollected(rebuyAddOnCollected);
    game.setAnnualTocCollected(annualTocCollected);
    game.setQuarterlyTocCollected(quarterlyTocCollected);

    totalCollected += buyInCollected + rebuyAddOnCollected + annualTocCollected + quarterlyTocCollected;
    game.setTotalCollected(totalCollected);
    game.setKittyCalculated(kittyCalculated);
    game.setAnnualTocFromRebuyAddOnCalculated(annualTocFromRebuyAddOnCalculated);
    game.setRebuyAddOnLessAnnualTocCalculated(rebuyAddOnCollected - annualTocFromRebuyAddOnCalculated);
    int totalTocCalculated = annualTocCollected + quarterlyTocCollected + annualTocFromRebuyAddOnCalculated;
    game.setTotalCombinedTocCalculated(totalTocCalculated);

    // prizePot = total collected minus total toc minus kitty
    game.setPrizePotCalculated(totalCollected - totalTocCalculated - kittyCalculated);
    game.setLastCalculated(LocalDateTime.now());
    gameRepository.save(game);
    return game;
  }
}
