package com.texastoc.service.calculator;

import com.texastoc.model.config.TocConfig;
import com.texastoc.model.game.Game;
import com.texastoc.model.game.GamePlayer;
import com.texastoc.repository.ConfigRepository;
import com.texastoc.repository.GameRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class GameCalculator {

  private final GameRepository gameRepository;
  private final ConfigRepository configRepository;
  private TocConfig tocConfig;

  public GameCalculator(GameRepository gameRepository, ConfigRepository configRepository) {
    this.gameRepository = gameRepository;
    this.configRepository = configRepository;
  }

  // TODO
  //public Game calculate(int gameId) {

  @SuppressWarnings("Duplicates")
  public Game calculate(Game game, List<GamePlayer> gamePlayers) {

    int numPlayers = 0;

    int buyInCollected = 0;
    int rebuyAddOnCollected = 0;
    int annualTocCollected = 0;
    int quarterlyTocCollected = 0;
    int totalCollected = 0;

    int kittyCalculated = 0;
    int annualTocFromRebuyAddOnCalculated = 0;

    for (GamePlayer gamePlayer : gamePlayers) {
      ++numPlayers;

      buyInCollected += gamePlayer.getBuyInCollected() == null ? 0 : gamePlayer.getBuyInCollected();
      rebuyAddOnCollected += gamePlayer.getRebuyAddOnCollected() == null ? 0 : gamePlayer.getRebuyAddOnCollected();
      annualTocCollected += gamePlayer.getAnnualTocCollected() == null ? 0 : gamePlayer.getAnnualTocCollected();
      quarterlyTocCollected += gamePlayer.getQuarterlyTocCollected() == null ? 0 : gamePlayer.getQuarterlyTocCollected();

      boolean isAnnualToc = gamePlayer.getAnnualTocCollected() != null && gamePlayer.getAnnualTocCollected() > 0;
      boolean isRebuyAddOn = gamePlayer.getRebuyAddOnCollected() != null && gamePlayer.getRebuyAddOnCollected() > 0;
      if (isAnnualToc && isRebuyAddOn) {
        annualTocFromRebuyAddOnCalculated += getTocConfig().getRegularRebuyTocDebit();
      }
    }

    if (buyInCollected > 0) {
      kittyCalculated = getTocConfig().getKittyDebit();
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

    gameRepository.update(game);

    return game;
  }

  // Cache it
  private TocConfig getTocConfig() {
    if (tocConfig == null) {
      tocConfig = configRepository.get();
    }
    return tocConfig;
  }
}
