package com.texastoc.module.game.service;

import com.texastoc.TestConstants;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.repository.GameRepository;
import com.texastoc.module.player.PlayerModule;
import com.texastoc.module.player.model.Player;
import com.texastoc.module.player.model.Role;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class GamePlayerServiceTest implements TestConstants {

  private GamePlayerService gamePlayerService;

  private GameRepository gameRepository;
  private GameHelper gameHelper;

  private PlayerModule playerModule;

  @Before
  public void init() {
    gameRepository = mock(GameRepository.class);
    gameHelper = mock(GameHelper.class);
    playerModule = mock(PlayerModule.class);
    gamePlayerService = new GamePlayerService(gameRepository, gameHelper);
    ReflectionTestUtils.setField(gamePlayerService, "playerModule", playerModule);
  }

  @Test
  public void testCreateGamePlayer() {

    // GameService#createGamePlayers calls
    // 1. gameHelper.get
    // 2. gameHelper.checkFinalized
    // 3. playerModule.get(id)
    // 4. gameRepository.save
    // Then calls recalculate on a separate thread
    // Not verifying the calculators because they have their own tests

    // Arrange
    Game currentGame = Game.builder()
      .id(1)
      .numPlayers(0)
      .finalized(false)
      .build();

    when(gameHelper.get(1)).thenReturn(currentGame);

    doNothing().when(gameHelper).checkFinalized(any());

    Player player = Player.builder()
      .id(1)
      .firstName("bob")
      .lastName("cob")
      .build();
    Mockito.when(playerModule.get(1)).thenReturn(player);

    GamePlayer gamePlayerToCreated = GamePlayer.builder()
      .gameId(1)
      .playerId(1)
      .build();

    // Act
    gamePlayerService.createGamePlayer(gamePlayerToCreated);

    // Assert
    ArgumentCaptor<Game> gameArg = ArgumentCaptor.forClass(Game.class);
    Mockito.verify(gameRepository).save(gameArg.capture());

    Game savedGame = gameArg.getValue();
    assertEquals(1, savedGame.getPlayers().size());
    GamePlayer savedGamePlayer = savedGame.getPlayers().get(0);

    assertEquals(1, savedGamePlayer.getGameId());
    assertEquals(1, savedGamePlayer.getPlayerId());
    assertEquals("bob cob", savedGamePlayer.getName());

    assertNewlyCreatedGamePlayer(savedGamePlayer);
  }

  @Test
  public void testFirstTimeGamePlayer() {

    // GameService#createFirstTimeGamePlayer calls
    // 1. gameHelper.get
    // 2. gameHelper.checkFinalized
    // 3. playerModule.create
    // 4. gameRepository.save
    // Then calls recalculate on a separate thread
    // Not verifying the calculators because they have their own tests

    // Arrange
    Game currentGame = Game.builder()
      .id(2)
      .numPlayers(0)
      .finalized(false)
      .build();

    when(gameHelper.get(2)).thenReturn(currentGame);

    doNothing().when(gameHelper).checkFinalized(any());

    when(playerModule.create(any(Player.class)))
      .thenReturn(Player.builder()
        .id(5)
        .build());

    GamePlayer firstTimeGamePlayer = GamePlayer.builder()
      .gameId(2)
      .firstName("John")
      .lastName("Doe")
      .email("johndoe@texastoc.com")
      .build();

    // Act
    gamePlayerService.createFirstTimeGamePlayer(firstTimeGamePlayer);

    // Assert
    ArgumentCaptor<Player> argument = ArgumentCaptor.forClass(Player.class);
    verify(playerModule).create(argument.capture());
    Player player = argument.getValue();
    assertEquals("John", player.getFirstName());
    assertEquals("Doe", player.getLastName());
    assertEquals("johndoe@texastoc.com", player.getEmail());
    assertThat(player.getRoles()).containsExactly(Role.builder()
      .type(Role.Type.USER)
      .build());

    ArgumentCaptor<Game> gameArg = ArgumentCaptor.forClass(Game.class);
    Mockito.verify(gameRepository).save(gameArg.capture());

    Game savedGame = gameArg.getValue();
    assertEquals(1, savedGame.getPlayers().size());
    GamePlayer savedGamePlayer = savedGame.getPlayers().get(0);

    assertEquals(2, savedGamePlayer.getGameId());
    assertEquals(5, savedGamePlayer.getPlayerId());
    assertEquals("John", savedGamePlayer.getFirstName());
    assertEquals("Doe", savedGamePlayer.getLastName());

    assertNewlyCreatedGamePlayer(savedGamePlayer);
  }


//  /**
//   * Somewhat of an anorexic test since there are no players but then again
//   * the game service code is just a pass through to the repositories.
//   */
//  @Test
//  public void getGameNoPlayers() {
//
//    Mockito.when(gameRepository.getById(1))
//      .thenReturn(Game.builder()
//        .id(1)
//        .build());
//
//    Mockito.when(gamePlayerRepository.selectByGameId(1))
//      .thenReturn(Collections.emptyList());
//
//    Mockito.when(gamePayoutRepository.getByGameId(1))
//      .thenReturn(Collections.emptyList());
//
//    Game game = gameService.get(1);
//
//    // Game repository called once
//    Mockito.verify(gameRepository, Mockito.times(1)).getById(1);
//    assertNotNull("Game returned should not be null", game);
//    assertEquals("Game id should be 1", 1, (int) game.getId());
//
//    // GamePlayer repository called once
//    Mockito.verify(gamePlayerRepository, Mockito.times(1)).selectByGameId(1);
//    assertNotNull("GamePlayers returned should not be null", game.getPlayers());
//    assertEquals("number of players should be 0", 0, game.getPlayers().size());
//
//    // GamePayout repository called once
//    Mockito.verify(gamePayoutRepository, Mockito.times(1)).getByGameId(1);
//    assertNotNull("GamePayouts returned should not be null", game.getPayouts());
//    assertEquals("number of payouts should be 0", 0, game.getPayouts().size());
//  }
//
//  @Test
//  public void testUpdateGamePlayer() {
//    // GameService#updateGamePlayer calls
//    // 1. gameRepository.getById
//    // 2. gamePlayerRepository.selectById(id)
//    // 3. gamePlayerRepository.update
//    // Then calls recalculate on a separate thread
//    // Not verifying the calculators because they have their own tests
//
//    Game currentGame = Game.builder()
//      .id(1)
//      .numPlayers(1)
//      .buyInCost(GAME_BUY_IN)
//      .annualTocCost(TOC_PER_GAME)
//      .quarterlyTocCost(QUARTERLY_TOC_PER_GAME)
//      .rebuyAddOnCost(GAME_REBUY)
//      .finalized(false)
//      .build();
//    // 1. gameRepository.getById
//    Mockito.when(gameRepository.getById(1)).thenReturn(currentGame);
//
//    GamePlayer gamePlayer = GamePlayer.builder()
//      .id(1)
//      .gameId(1)
//      .build();
//    // 2. gamePlayerRepository.selectById(id)
//    Mockito.when(gamePlayerRepository.selectById(1)).thenReturn(gamePlayer);
//
//    // Same as game player
//    UpdateGamePlayerRequest ugpr = UpdateGamePlayerRequest.builder()
//      .buyInCollected(true)
//      .rebuyAddOnCollected(true)
//      .annualTocCollected(true)
//      .quarterlyTocCollected(true)
//      .roundUpdates(true)
//      .place(10)
//      .knockedOut(true)
//      .chop(500)
//      .build();
//
//    GamePlayer gamePlayerUpdated = gameService.updateGamePlayer(1, 1, ugpr);
//
//    Mockito.verify(gameRepository, Mockito.times(1)).getById(1);
//    Mockito.verify(gamePlayerRepository, Mockito.times(1)).selectById(1);
//    Mockito.verify(gamePlayerRepository, Mockito.times(1)).update(any(GamePlayer.class));
//
//    assertNotNull("game player updated should not be null", gamePlayerUpdated);
//    assertEquals("game player id should be 1", 1, gamePlayerUpdated.getGameId());
//    assertEquals("game player id should be 1", 1, gamePlayerUpdated.getId());
//
//    assertNull("game player points should be null", gamePlayerUpdated.getPoints());
//
//    assertEquals("game player finish should be 10", 10, gamePlayerUpdated.getPlace().intValue());
//    assertTrue("game player knocked out should be true", gamePlayerUpdated.getKnockedOut());
//    assertTrue("game player round updates should be true", gamePlayerUpdated.getRoundUpdates());
//    assertEquals("game player buy-in collected should be " + GAME_BUY_IN, GAME_BUY_IN, gamePlayerUpdated.getBuyInCollected().intValue());
//    assertEquals("game player rebuy add on collected should be " + GAME_REBUY, GAME_REBUY, gamePlayerUpdated.getRebuyAddOnCollected().intValue());
//    assertEquals("game player annual toc collected should be " + TOC_PER_GAME, TOC_PER_GAME, gamePlayerUpdated.getAnnualTocCollected().intValue());
//    assertEquals("game player quarterly toc collected should be " + QUARTERLY_TOC_PER_GAME, QUARTERLY_TOC_PER_GAME, gamePlayerUpdated.getQuarterlyTocCollected().intValue());
//    assertEquals("game player chop should be 500", 500, gamePlayerUpdated.getChop().intValue());
//  }
//
//  @Test
//  public void testDeleteGamePlayer() {
//
//    GamePlayer gamePlayer = GamePlayer.builder()
//      .id(1)
//      .gameId(1)
//      .build();
//    Mockito.when(gamePlayerRepository.selectById(1)).thenReturn(gamePlayer);
//
//    Mockito.doNothing().when(gamePlayerRepository).deleteById(1, 1);
//
//    Game currentGame = Game.builder()
//      .id(1)
//      .numPlayers(0)
//      .build();
//    Mockito.when(gameRepository.getById(1)).thenReturn(currentGame);
//
//    gameService.deleteGamePlayer(1, 1);
//
//    Mockito.verify(gamePlayerRepository, Mockito.times(1)).selectById(1);
//    Mockito.verify(gamePlayerRepository, Mockito.times(1)).deleteById(1, 1);
//    Mockito.verify(gameRepository, Mockito.times(2)).getById(1);
//  }
//

  public void assertNewlyCreatedGamePlayer(GamePlayer gamePlayer) {

    assertNull("game player points should be null", gamePlayer.getPoints());
    assertNull("game player finish should be null", gamePlayer.getPlace());
    assertFalse("game player knocked out should be false", gamePlayer.isKnockedOut());
    assertFalse("game player round updates should be false", gamePlayer.isRoundUpdates());
    assertFalse("game player buy-in collected should be false", gamePlayer.isBuyInCollected());
    assertFalse("game player rebuy add on collected should be false", gamePlayer.isRebuyAddOnCollected());
    assertFalse("game player annual toc collected should be false", gamePlayer.isAnnualTocCollected());
    assertFalse("game player quarterly toc collected should be false", gamePlayer.isQuarterlyTocCollected());
    assertNull("game player chop should be null", gamePlayer.getChop());
  }

}
