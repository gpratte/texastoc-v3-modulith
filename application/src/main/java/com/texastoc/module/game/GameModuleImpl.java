package com.texastoc.module.game;

import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.model.Seating;
import com.texastoc.module.game.service.GameService;
import com.texastoc.module.game.service.SeatingService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GameModuleImpl implements GameModule {

  private final GameService gameService;
  private final SeatingService seatingService;

  public GameModuleImpl(GameService gameService, SeatingService seatingService) {
    this.gameService = gameService;
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
    return gameService.createGamePlayer(gamePlayer);
  }

  @Override
  public GamePlayer createFirstTimeGamePlayer(GamePlayer gamePlayer) {
    return gameService.createFirstTimeGamePlayer(gamePlayer);
  }

  @Override
  public void updateGamePlayer(GamePlayer gamePlayer) {
    gameService.updateGamePlayer(gamePlayer);
  }

  @Override
  public void toggleGamePlayerKnockedOut(int gameId, int gamePlayerId) {
    gameService.toggleGamePlayerKnockedOut(gameId, gamePlayerId);
  }

  @Override
  public void toggleGamePlayerRebuy(int gameId, int gamePlayerId) {
    gameService.toggleGamePlayerRebuy(gameId, gamePlayerId);
  }

  @Override
  public void deleteGamePlayer(int gameId, int gamePlayerId) {
    gameService.deleteGamePlayer(gameId, gamePlayerId);
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
