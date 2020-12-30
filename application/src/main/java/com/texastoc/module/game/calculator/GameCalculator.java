package com.texastoc.module.game.calculator;

import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.repository.GameRepository;
import com.texastoc.module.season.SeasonService;
import com.texastoc.module.season.model.Season;
import com.texastoc.module.settings.SettingsModuleFactory;
import com.texastoc.module.settings.model.Settings;
import com.texastoc.module.settings.model.TocConfig;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class GameCalculator {

  private final GameRepository gameRepository;
  private final SeasonService seasonService;
  private TocConfig tocConfig;

  public GameCalculator(GameRepository gameRepository, SeasonService seasonService) {
    this.gameRepository = gameRepository;
    this.seasonService = seasonService;
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

    for (GamePlayer gamePlayer : game.getPlayers()) {
      ++numPlayers;
      buyInCollected += gamePlayer.getBuyInCollected() == null ? 0 : gamePlayer.getBuyInCollected();
      rebuyAddOnCollected += gamePlayer.getRebuyAddOnCollected() == null ? 0 : gamePlayer.getRebuyAddOnCollected();
      annualTocCollected += gamePlayer.getAnnualTocCollected() == null ? 0 : gamePlayer.getAnnualTocCollected();
      quarterlyTocCollected += gamePlayer.getQuarterlyTocCollected() == null ? 0 : gamePlayer.getQuarterlyTocCollected();

      boolean isAnnualToc = gamePlayer.getAnnualTocCollected() != null && gamePlayer.getAnnualTocCollected() > 0;
      boolean isRebuyAddOn = gamePlayer.getRebuyAddOnCollected() != null && gamePlayer.getRebuyAddOnCollected() > 0;
      if (isAnnualToc && isRebuyAddOn) {
        annualTocFromRebuyAddOnCalculated += getTocConfig(game.getSeasonId()).getRegularRebuyTocDebit();
      }
    }

    if (buyInCollected > 0) {
      kittyCalculated = getTocConfig(game.getSeasonId()).getKittyDebit();
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

  // Cache it
  private TocConfig getTocConfig(int seasonId) {
    if (tocConfig == null) {
      Season season = seasonService.getSeason(seasonId);
      Settings settings = SettingsModuleFactory.getSettingsModule().get();
      tocConfig = settings.getTocConfigs().get(season.getStart().getYear());
    }
    return tocConfig;
  }
}
