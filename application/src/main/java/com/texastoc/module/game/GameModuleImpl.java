package com.texastoc.module.game;

import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.model.Seating;
import com.texastoc.module.game.service.GamePlayerService;
import com.texastoc.module.game.service.GameService;
import com.texastoc.module.game.service.SeatingService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GameModuleImpl implements GameModule {

  private final GameService gameService;
  private final GamePlayerService gamePlayerService;
  private final SeatingService seatingService;

  public GameModuleImpl(GameService gameService, GamePlayerService gamePlayerService, SeatingService seatingService) {
    this.gameService = gameService;
    this.gamePlayerService = gamePlayerService;
    this.seatingService = seatingService;
  }

  @Override
  public Game create(Game game) {
    return gameService.create(game);
  }

  @Override
  public void update(Game game) {
    gameService.update(game);
  }

  @Override
  public Game get(int id) {
    return gameService.get(id);
  }

  @Override
  public Game getCurrent() {
    return gameService.getCurrent();
  }

  @Override
  public List<Game> getBySeasonId(Integer seasonId) {
    return gameService.getBySeasonId(seasonId);
  }

  @Override
  public List<Game> getByQuarterlySeasonId(Integer qSeasonId) {
    return gameService.getByQuarterlySeasonId(qSeasonId);
  }

  @Override
  public void finalize(int id) {
    gameService.finalize(id);
  }

  @Override
  public void unfinalize(int id) {
    gameService.unfinalize(id);
  }

  @Override
  public GamePlayer createGamePlayer(GamePlayer gamePlayer) {
    return gamePlayerService.createGamePlayer(gamePlayer);
  }

  @Override
  public GamePlayer createFirstTimeGamePlayer(GamePlayer gamePlayer) {
    return gamePlayerService.createFirstTimeGamePlayer(gamePlayer);
  }

  @Override
  public void updateGamePlayer(GamePlayer gamePlayer) {
    gamePlayerService.updateGamePlayer(gamePlayer);
  }

  @Override
  public void toggleGamePlayerKnockedOut(int gameId, int gamePlayerId) {
    gamePlayerService.toggleGamePlayerKnockedOut(gameId, gamePlayerId);
  }

  @Override
  public void toggleGamePlayerRebuy(int gameId, int gamePlayerId) {
    gamePlayerService.toggleGamePlayerRebuy(gameId, gamePlayerId);
  }

  @Override
  public void deleteGamePlayer(int gameId, int gamePlayerId) {
    gamePlayerService.deleteGamePlayer(gameId, gamePlayerId);
  }

  @Override
  public Seating seatGamePlayers(Seating seating) {
    return seatingService.seatGamePlayers(seating);
  }

  @Override
  public void notifySeating(int gameId) {
    seatingService.notifySeating(gameId);
  }
}
