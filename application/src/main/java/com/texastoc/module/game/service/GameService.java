package com.texastoc.module.game.service;

import com.texastoc.module.game.exception.GameInProgressException;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.repository.GameRepository;
import com.texastoc.module.player.PlayerModule;
import com.texastoc.module.player.PlayerModuleFactory;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.quarterly.QuarterlySeasonModule;
import com.texastoc.module.quarterly.QuarterlySeasonModuleFactory;
import com.texastoc.module.quarterly.model.QuarterlySeason;
import com.texastoc.module.season.SeasonModule;
import com.texastoc.module.season.SeasonModuleFactory;
import com.texastoc.module.season.model.Season;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class GameService {

  private final GameRepository gameRepository;
  private final GameHelper gameHelper;

  private PlayerModule playerModule;
  private SeasonModule seasonModule;
  private QuarterlySeasonModule quarterlySeasonModule;

  public GameService(GameRepository gameRepository, GameHelper gameHelper) {
    this.gameRepository = gameRepository;
    this.gameHelper = gameHelper;
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public Game create(Game game) {
    // TODO bean validation https://www.baeldung.com/javax-validation
    Season currentSeason = getSeasonModule().getCurrentSeason();

    // TODO check that date is allowed - not before an existing game and not beyond the season.

    // Make sure no other game is open
    List<Game> otherGames = gameRepository.findBySeasonId(currentSeason.getId());
    for (Game otherGame : otherGames) {
      if (!otherGame.isFinalized()) {
        throw new GameInProgressException("There is a game in progress");
      }
    }

    Player player = getPlayerModule().get(game.getHostId());
    game.setHostName(player.getName());

    // Game setup variables
    game.setSeasonId(currentSeason.getId());
    game.setKittyCost(currentSeason.getKittyPerGame());
    game.setBuyInCost(currentSeason.getBuyInCost());
    game.setRebuyAddOnCost(currentSeason.getRebuyAddOnCost());
    game.setRebuyAddOnTocDebitCost(currentSeason.getRebuyAddOnTocDebit());
    game.setAnnualTocCost(currentSeason.getTocPerGame());
    game.setQuarterlyTocCost(currentSeason.getQuarterlyTocPerGame());
    game.setSeasonGameNum(currentSeason.getNumGamesPlayed() + 1);

    game.setBuyInCost(currentSeason.getBuyInCost());
    game.setRebuyAddOnCost(currentSeason.getRebuyAddOnCost());
    game.setRebuyAddOnTocDebitCost(currentSeason.getRebuyAddOnTocDebit());

    QuarterlySeason currentQSeason = getQuarterlySeasonModule()
        .getQuarterlySeasonByDate(game.getDate());
    game.setQSeasonId(currentQSeason.getId());
    game.setQuarter(currentQSeason.getQuarter());
    game.setQuarterlyGameNum(currentQSeason.getNumGamesPlayed() + 1);

    game = gameRepository.save(game);

    gameHelper.sendUpdatedGame();
    return game;
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public void update(Game game) {
    // TODO bean validation https://www.baeldung.com/javax-validation
    Game currentGame = get(game.getId());
    gameHelper.checkFinalized(currentGame);
    currentGame.setHostId(game.getHostId());
    currentGame.setDate(game.getDate());
    currentGame.setTransportRequired(game.isTransportRequired());
    gameRepository.save(currentGame);
    gameHelper.sendUpdatedGame();
  }

  @Transactional(readOnly = true)
  public Game get(int id) {
    return gameHelper.get(id);
  }

  @Transactional(readOnly = true)
  @Cacheable("currentGame")
  public Game getCurrent() {
    return gameHelper.getCurrent();
  }


  @CacheEvict(value = "currentGame", allEntries = true)
  public void clearCacheGame() {
  }

  @Transactional(readOnly = true)
  public List<Game> getBySeasonId(Integer seasonId) {
    if (seasonId == null) {
      seasonId = getSeasonModule().getCurrentSeasonId();
    }

    return gameRepository.findBySeasonId(seasonId);
  }

  @Transactional(readOnly = true)
  public List<Game> getByQuarterlySeasonId(Integer qSeasonId) {
    return gameRepository.findByQuarterlySeasonId(qSeasonId);
  }

  @CacheEvict(value = {"currentGame", "currentSeason",
      "currentSeasonById"}, allEntries = true, beforeInvocation = false)
  @Transactional
  public void finalize(int id) {
    // TODO check that the game has the appropriate finishes (e.g. 1st, 2nd, ...)
    Game game = get(id);

    if (game.isFinalized()) {
      return;
    }

    gameHelper.recalculate(game.getId());
    game = get(id);
    game.setFinalized(true);
    game.setSeating(null);
    // TODO set game.chopped
    gameRepository.save(game);
    // TODO message season for it to recalculate season and quarter
    gameHelper.sendUpdatedGame();
    // TODO message clock to end
    gameHelper.sendGameSummary(id);
  }

  @CacheEvict(value = {"currentGame", "currentSeason",
      "currentSeasonById"}, allEntries = true, beforeInvocation = false)
  public void unfinalize(int id) {
    // TODO admin only
    Game gameToOpen = get(id);

    if (!gameToOpen.isFinalized()) {
      return;
    }

    Season season = getSeasonModule().getSeason(gameToOpen.getSeasonId());
    if (season.isFinalized()) {
      // TODO throw a unique exception and handle in controller
      throw new RuntimeException("Cannot open a game when season is finalized");
    }

    // Make sure no other game is open
    List<Game> games = gameRepository.findBySeasonId(gameToOpen.getSeasonId());
    for (Game game : games) {
      if (game.getId() == gameToOpen.getId()) {
        continue;
      }
      if (!game.isFinalized()) {
        throw new GameInProgressException("There is a game in progress");
      }
    }

    gameToOpen.setFinalized(false);
    gameRepository.save(gameToOpen);
    gameHelper.sendUpdatedGame();
  }

  private PlayerModule getPlayerModule() {
    if (playerModule == null) {
      playerModule = PlayerModuleFactory.getPlayerModule();
    }
    return playerModule;
  }

  private SeasonModule getSeasonModule() {
    if (seasonModule == null) {
      seasonModule = SeasonModuleFactory.getSeasonModule();
    }
    return seasonModule;
  }

  private QuarterlySeasonModule getQuarterlySeasonModule() {
    if (quarterlySeasonModule == null) {
      quarterlySeasonModule = QuarterlySeasonModuleFactory.getQuarterlySeasonModule();
    }
    return quarterlySeasonModule;
  }
}
