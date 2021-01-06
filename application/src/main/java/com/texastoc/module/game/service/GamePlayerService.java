package com.texastoc.module.game.service;

import com.google.common.collect.ImmutableSet;
import com.texastoc.exception.NotFoundException;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.repository.GameRepository;
import com.texastoc.module.player.PlayerModule;
import com.texastoc.module.player.PlayerModuleFactory;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GamePlayerService {

  private final GameRepository gameRepository;
  private final GameHelper gameHelper;

  private PlayerModule playerModule;

  public GamePlayerService(GameRepository gameRepository, GameHelper gameHelper) {
    this.gameRepository = gameRepository;
    this.gameHelper = gameHelper;
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public GamePlayer createGamePlayer(GamePlayer gamePlayer) {
    // TODO bean validation https://www.baeldung.com/javax-validation
    Game game = gameHelper.get(gamePlayer.getGameId());
    gameHelper.checkFinalized(game);

    GamePlayer gamePlayerCreated = createGamePlayerWorker(gamePlayer, game);
    gameHelper.sendUpdatedGame();
    return gamePlayerCreated;
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public GamePlayer createFirstTimeGamePlayer(GamePlayer gamePlayer) {
    // TODO bean validation https://www.baeldung.com/javax-validation
    Game game = gameHelper.get(gamePlayer.getGameId());
    gameHelper.checkFinalized(game);

    String firstName = gamePlayer.getFirstName();
    String lastName = gamePlayer.getLastName();
    Player player = Player.builder()
      .firstName(firstName)
      .lastName(lastName)
      .email(gamePlayer.getEmail())
      .roles(ImmutableSet.of(Role.builder()
        .type(Role.Type.USER)
        .build()))
      .build();
    int playerId = getPlayerModule().create(player).getId();
    gamePlayer.setPlayerId(playerId);

    GamePlayer gamePlayerCreated = createGamePlayerWorker(gamePlayer, game);
    gameHelper.sendUpdatedGame();
    return gamePlayerCreated;
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public void updateGamePlayer(GamePlayer gamePlayer) {
    Game game = gameHelper.get(gamePlayer.getGameId());
    gameHelper.checkFinalized(game);

    GamePlayer existingGamePlayer = game.getPlayers().stream()
      .filter(gp -> gp.getId() == gamePlayer.getId())
      .findFirst().get();

    existingGamePlayer.setPlace(gamePlayer.getPlace());
    existingGamePlayer.setRoundUpdates(gamePlayer.isRoundUpdates());
    existingGamePlayer.setBuyInCollected(gamePlayer.isBuyInCollected());
    existingGamePlayer.setRebuyAddOnCollected(gamePlayer.isRebuyAddOnCollected());
    existingGamePlayer.setAnnualTocCollected(gamePlayer.isAnnualTocCollected());
    existingGamePlayer.setQuarterlyTocCollected(gamePlayer.isQuarterlyTocCollected());
    existingGamePlayer.setChop(gamePlayer.getChop());

    if (gamePlayer.getPlace() != null && gamePlayer.getPlace() <= 10) {
      existingGamePlayer.setKnockedOut(true);
    } else {
      existingGamePlayer.setKnockedOut(gamePlayer.isKnockedOut());
    }

    gameRepository.save(game);
    gameHelper.recalculate(game);
    gameHelper.sendUpdatedGame();
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public void toggleGamePlayerKnockedOut(int gameId, int gamePlayerId) {
    Game game = gameHelper.get(gameId);
    gameHelper.checkFinalized(game);

    Optional<GamePlayer> optionalGamePlayer = game.getPlayers().stream()
      .filter(gp -> gp.getId() == gamePlayerId)
      .findFirst();
    if (!optionalGamePlayer.isPresent()) {
      throw new NotFoundException("Game player with id " + gamePlayerId + " not found");
    }
    GamePlayer gamePlayer = optionalGamePlayer.get();
    gamePlayer.setKnockedOut(!gamePlayer.isKnockedOut());
    gameRepository.save(game);
    gameHelper.recalculate(game);
    gameHelper.sendUpdatedGame();
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public void toggleGamePlayerRebuy(int gameId, int gamePlayerId) {
    Game game = gameHelper.get(gameId);
    gameHelper.checkFinalized(game);

    Optional<GamePlayer> optionalGamePlayer = game.getPlayers().stream()
      .filter(gp -> gp.getId() == gamePlayerId)
      .findFirst();
    if (!optionalGamePlayer.isPresent()) {
      throw new NotFoundException("Game player with id " + gamePlayerId + " not found");
    }
    GamePlayer gamePlayer = optionalGamePlayer.get();
    gamePlayer.setRebuyAddOnCollected(!gamePlayer.isRebuyAddOnCollected());
    gameRepository.save(game);
    gameHelper.recalculate(game);
    gameHelper.sendUpdatedGame();
  }

  @CacheEvict(value = "currentGame", allEntries = true, beforeInvocation = false)
  @Transactional
  public void deleteGamePlayer(int gameId, int gamePlayerId) {
    Game game = gameHelper.get(gameId);
    gameHelper.checkFinalized(game);

    if (game.getPlayers() != null) {
      // Remove the game player from the list of game players
      game.setPlayers(game.getPlayers().stream()
        .filter(gp -> gp.getId() != gamePlayerId)
        .collect(Collectors.toList()));
      gameRepository.save(game);
      gameHelper.recalculate(game);
      gameHelper.sendUpdatedGame();
    }
  }

  private GamePlayer createGamePlayerWorker(GamePlayer gamePlayer, Game game) {
    if (gamePlayer.getFirstName() == null && gamePlayer.getLastName() == null) {
      Player player = getPlayerModule().get(gamePlayer.getPlayerId());
      gamePlayer.setFirstName(player.getFirstName());
      gamePlayer.setLastName(player.getLastName());
    }
    gamePlayer.setQSeasonId(game.getQSeasonId());
    gamePlayer.setSeasonId(game.getSeasonId());

    if (game.getPlayers() == null) {
      game.setPlayers(new ArrayList<>(1));
    }
    game.getPlayers().add(gamePlayer);
    // TODO verify the game player id gets set by spring data jdbc
    gameRepository.save(game);
    gameHelper.recalculate(game);
    return gamePlayer;
  }

  private PlayerModule getPlayerModule() {
    if (playerModule == null) {
      playerModule = PlayerModuleFactory.getPlayerModule();
    }
    return playerModule;
  }

}
