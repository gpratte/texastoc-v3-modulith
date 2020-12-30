package com.texastoc.module.game;

import com.texastoc.module.game.exception.GameInProgressException;
import com.texastoc.module.game.exception.GameIsFinalizedException;
import com.texastoc.module.game.model.FirstTimeGamePlayer;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.model.Seating;
import com.texastoc.module.game.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
public class GameRestController {

  private final GameModule gameModule;
  private final GameService gameService;

  public GameRestController(GameModuleImpl gameModuleImpl, GameService gameService) {
    gameModule = gameModuleImpl;
    this.gameService = gameService;
  }

  @PostMapping(value = "/api/v2/games", consumes = MediaType.APPLICATION_JSON_VALUE)
  public Game createGame(@RequestBody Game game) {
    return gameModule.create(game);
  }

  @PatchMapping(value = "/api/v2/games/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public void updateGame(@PathVariable("id") int id, @RequestBody Game game) {
    game.setId(id);
    gameModule.update(game);
  }

  @GetMapping("/api/v2/games/{id}")
  public Game getGame(@PathVariable("id") int id) {
    return gameModule.get(id);
  }

  @GetMapping(value = "/api/v2/games", consumes = "application/vnd.texastoc.current+json")
  public Game getCurrentGame() {
    return gameModule.getCurrent();
  }

  // TODO this needs to go away
  @GetMapping(value = "/api/v2/games", consumes = "application/vnd.texastoc.clear-cache+json")
  public String getCurrentNoCacheGame() {
    gameService.geClearCacheGame();
    return "done";
  }

  @GetMapping("/api/v2/games")
  public List<Game> getGamesBySeasonId(@RequestParam(required = false) Integer seasonId) {
    return gameModule.getBySeasonId(seasonId);
  }

  @PutMapping(value = "/api/v2/games/{id}", consumes = "application/vnd.texastoc.finalize+json")
  public void finalizeGame(@PathVariable("id") int id) {
    gameModule.finalize(id);
  }

  @PutMapping(value = "/api/v2/games/{id}", consumes = "application/vnd.texastoc.unfinalize+json")
  public void unfinalizeGame(@PathVariable("id") int id) {
    gameModule.unfinalize(id);
  }

  @PostMapping(value = "/api/v2/games/{id}/players", consumes = MediaType.APPLICATION_JSON_VALUE)
  public GamePlayer createGamePlayer(@PathVariable("id") int id, @RequestBody GamePlayer gamePlayer) {
    return gameModule.createGamePlayer(gamePlayer);
  }

  @PostMapping(value = "/api/v2/games/{id}/players", consumes = "application/vnd.texastoc.new-player+json")
  public GamePlayer createFirstTimeGamePlayer(@PathVariable("id") int id, @RequestBody @Valid FirstTimeGamePlayer firstTimeGamePlayer) {
    Game game = gameModule.get(id);
    GamePlayer gamePlayer = GamePlayer.builder()
      .gameId(id)
      .firstName(firstTimeGamePlayer.getFirstName())
      .lastName(firstTimeGamePlayer.getLastName())
      .email(firstTimeGamePlayer.getEmail())
      .buyInCollected(firstTimeGamePlayer.isBuyInCollected() ? game.getBuyInCost() : null)
      .annualTocCollected(firstTimeGamePlayer.isAnnualTocCollected() ? game.getAnnualTocCost() : null)
      .quarterlyTocCollected(firstTimeGamePlayer.isQuarterlyTocCollected() ? game.getQuarterlyTocCost() : null)
      .build();
    return gameModule.createFirstTimeGamePlayer(gamePlayer);
  }

  @PatchMapping(value = "/api/v2/games/{gameId}/players/{gamePlayerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public void updateGamePlayer(@PathVariable("gameId") int gameId, @PathVariable("gamePlayerId") int gamePlayerId, @RequestBody GamePlayer gamePlayer) {
    gamePlayer.setPlayerId(gamePlayerId);
    gamePlayer.setGameId(gameId);
    gameModule.updateGamePlayer(gamePlayer);
  }

  @PutMapping(value = "/api/v2/games/{gameId}/players/{gamePlayerId}", consumes = "application/vnd.texastoc.knockout+json")
  public void toggleKnockedOut(@PathVariable("gameId") int gameId, @PathVariable("gamePlayerId") int gamePlayerId) {
    gameModule.toggleGamePlayerKnockedOut(gameId, gamePlayerId);
  }

  @PutMapping(value = "/api/v2/games/{gameId}/players/{gamePlayerId}", consumes = "application/vnd.texastoc.rebuy+json")
  public void toggleRebuy(@PathVariable("gameId") int gameId, @PathVariable("gamePlayerId") int gamePlayerId) {
    gameModule.toggleGamePlayerRebuy(gameId, gamePlayerId);
  }

  @DeleteMapping("/api/v2/games/{gameId}/players/{gamePlayerId}")
  public void deleteGamePlayer(@PathVariable("gameId") int gameId, @PathVariable("gamePlayerId") int gamePlayerId) {
    gameService.deleteGamePlayer(gameId, gamePlayerId);
  }

  @PostMapping(value = "/api/v2/games/{gameId}/seats", consumes = "application/vnd.texastoc.assign-seats+json")
  public Seating seating(@PathVariable("gameId") int gameId, @RequestBody Seating seating) {
    seating.setGameId(gameId);
    return gameModule.seatGamePlayers(seating);
  }

  @PostMapping(value = "/api/v2/games/{gameId}/seats", consumes = "application/vnd.texastoc.notify-seats+json")
  public void notifySeating(@PathVariable("gameId") int gameId) {
    gameModule.notifySeating(gameId);
  }

  @ExceptionHandler(value = {GameInProgressException.class})
  protected void handleGameInProgressException(GameInProgressException ex, HttpServletResponse response) throws IOException {
    response.sendError(HttpStatus.CONFLICT.value(), ex.getMessage());
  }

  @ExceptionHandler(value = {GameIsFinalizedException.class})
  protected void handleFinalizedException(GameIsFinalizedException ex, HttpServletResponse response) throws IOException {
    response.sendError(HttpStatus.CONFLICT.value(), ex.getMessage());
  }

}
