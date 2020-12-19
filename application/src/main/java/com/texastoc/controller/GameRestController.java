package com.texastoc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.texastoc.controller.request.*;
import com.texastoc.exception.NotFoundException;
import com.texastoc.model.game.FirstTimeGamePlayer;
import com.texastoc.model.game.Game;
import com.texastoc.model.game.GamePlayer;
import com.texastoc.model.game.Seating;
import com.texastoc.service.ClockService;
import com.texastoc.service.GameService;
import com.texastoc.service.SeatingService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@SuppressWarnings("unused")
@RestController
public class GameRestController {

  private final GameService gameService;
  private final SeatingService seatingService;
  private final ClockService clockService;

  public GameRestController(GameService gameService, SeatingService seatingService, ClockService clockService) {
    this.gameService = gameService;
    this.seatingService = seatingService;
    this.clockService = clockService;
  }

  @PostMapping(value = "/api/v2/games", consumes = MediaType.APPLICATION_JSON_VALUE)
  public Game createGame(@RequestBody @Valid CreateGameRequest createGameRequest) {
    return gameService.createGame(Game.builder()
      .hostId(createGameRequest.getHostId())
      .date(createGameRequest.getDate())
      .transportRequired(createGameRequest.getTransportRequired())
      .build());
  }

  @PutMapping(value = "/api/v2/games/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public void updateGame(@PathVariable("id") int id, @RequestBody @Valid UpdateGameRequest updateGameRequest) {
    Game game = gameService.getGame(id);
    game.setHostId(updateGameRequest.getHostId());
    game.setDate(updateGameRequest.getDate());
    game.setTransportRequired(updateGameRequest.getTransportRequired());
    game.setPayoutDelta(updateGameRequest.getPayoutDelta() == null ? 0 : updateGameRequest.getPayoutDelta());

    gameService.updateGame(game);
  }

  @GetMapping("/api/v2/games/{id}")
  public Game getGame(@PathVariable("id") int id) {
    return gameService.getGame(id);
  }

  @GetMapping(value = "/api/v2/games", consumes = "application/vnd.texastoc.current+json")
  public Game getCurrentGame() {
    Game game = gameService.getCurrentGame();
    if (game != null) {
      return game;
    }
    throw new NotFoundException("Current game not found");
  }

  // TODO PUT not GET
  @GetMapping(value = "/api/v2/games", consumes = "application/vnd.texastoc.clear-cache+json")
  public String getCurrentNoCacheGame() {
    gameService.geClearCacheGame();
    return "done";
  }

  @GetMapping("/api/v2/games")
  public List<Game> getGames(@RequestParam(required = false) Integer seasonId) {
    return gameService.getGames(seasonId);
  }

  @PutMapping(value = "/api/v2/games/{id}", consumes = "application/vnd.texastoc.finalize+json")
  public void finalizeGame(@PathVariable("id") int id) {
    clockService.endClock(id);
    gameService.endGame(id);
    gameService.sendSummary(id);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(value = "/api/v2/games/{id}", consumes = "application/vnd.texastoc.unfinalize+json")
  public void unfinalizeGame(@PathVariable("id") int id) {
    gameService.openGame(id);
  }


  @PostMapping(value = "/api/v2/games/{id}/players", consumes = MediaType.APPLICATION_JSON_VALUE)
  public GamePlayer createGamePlayer(@PathVariable("id") int id, @RequestBody @Valid CreateGamePlayerRequest cgpr) {
    return gameService.createGamePlayer(id, cgpr);
  }

  @PostMapping(value = "/api/v2/games/{id}/players", consumes = "application/vnd.texastoc.new-player+json")
  public GamePlayer createGamePlayer(@PathVariable("id") int id, @RequestBody @Valid FirstTimeGamePlayer firstTimeGamePlayer) {
    return gameService.createFirstTimeGamePlayer(id, firstTimeGamePlayer);
  }

  @PutMapping(value = "/api/v2/games/{gameId}/players/{gamePlayerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public GamePlayer updateGamePlayer(@PathVariable("gameId") int gameId, @PathVariable("gamePlayerId") int gamePlayerId, @RequestBody @Valid UpdateGamePlayerRequest ugpr) {
    return gameService.updateGamePlayer(gameId, gamePlayerId, ugpr);
  }

  @PutMapping(value = "/api/v2/games/{gameId}/players/{gamePlayerId}", consumes = "application/vnd.texastoc.knockout+json")
  public GamePlayer toggleKnockedOut(@PathVariable("gameId") int gameId, @PathVariable("gamePlayerId") int gamePlayerId) {
    return gameService.toogleGamePlayerKnockedOut(gameId, gamePlayerId);
  }

  @PutMapping(value = "/api/v2/games/{gameId}/players/{gamePlayerId}", consumes = "application/vnd.texastoc.rebuy+json")
  public GamePlayer toggleRebuy(@PathVariable("gameId") int gameId, @PathVariable("gamePlayerId") int gamePlayerId) {
    return gameService.toogleGamePlayerRebuy(gameId, gamePlayerId);
  }

  @DeleteMapping("/api/v2/games/{gameId}/players/{gamePlayerId}")
  public void deleteGamePlayer(@PathVariable("gameId") int gameId, @PathVariable("gamePlayerId") int gamePlayerId) {
    gameService.deleteGamePlayer(gameId, gamePlayerId);
  }

  @PostMapping(value = "/api/v2/games/{gameId}/seats", consumes = "application/vnd.texastoc.assign-seats+json")
  public Seating seating(@PathVariable("gameId") int gameId, @RequestBody SeatingRequest seatingRequest) throws JsonProcessingException {
    return seatingService.seat(gameId, seatingRequest.getNumSeatsPerTable(), seatingRequest.getTableRequests());
  }

  @PostMapping(value = "/api/v2/games/{gameId}/seats", consumes = "application/vnd.texastoc.notify-seats+json")
  public void notifySeating(@PathVariable("gameId") int gameId) throws JsonProcessingException {
    gameService.notifySeating(gameId);
  }

}
