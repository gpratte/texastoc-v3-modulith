package com.texastoc.module.game.service;

import com.texastoc.TestConstants;
import com.texastoc.exception.NotFoundException;
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

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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

  @Test
  public void testUpdateGamePlayer() {
    // GameService#updateGamePlayer calls
    // 1. gameHelper.get
    // 2. gameHelper.checkFinalized
    // 3. gameRepository.save
    // Then calls recalculate on a separate thread
    // Not verifying the calculators because they have their own tests

    // Arrange
    GamePlayer existingGamePlayer = GamePlayer.builder()
      .id(11)
      .playerId(22)
      .qSeasonId(33)
      .seasonId(44)
      .gameId(55)
      .firstName("existingFirstName")
      .lastName("existingLastName")
      .email("existing@mail.com")
      .place(null)
      .tocPoints(null)
      .roundUpdates(false)
      .boughtIn(false)
      .rebought(false)
      .annualTocParticipant(false)
      .quarterlyTocParticipant(false)
      .knockedOut(false)
      .chop(null)
      .build();

    Game currentGame = Game.builder()
      .id(55)
      .numPlayers(1)
      .finalized(false)
      .players(Arrays.asList(existingGamePlayer))
      .build();

    when(gameHelper.get(55)).thenReturn(currentGame);

    doNothing().when(gameHelper).checkFinalized(any());

    // Same as game player
    GamePlayer gamePlayerToUpdate = GamePlayer.builder()
      .id(11)
      .gameId(55)
      .firstName("updatedFirstName")
      .lastName("updatedLastName")
      .email("updated@mail.com")
      .place(10)
      .tocPoints(100)
      .roundUpdates(true)
      .boughtIn(true)
      .rebought(true)
      .annualTocParticipant(true)
      .quarterlyTocParticipant(true)
      .knockedOut(true)
      .chop(1000)
      .build();
    gamePlayerService.updateGamePlayer(gamePlayerToUpdate);

    ArgumentCaptor<Game> gameArg = ArgumentCaptor.forClass(Game.class);
    Mockito.verify(gameRepository).save(gameArg.capture());

    Game savedGame = gameArg.getValue();
    assertEquals(1, savedGame.getPlayers().size());
    GamePlayer savedGamePlayer = savedGame.getPlayers().get(0);

    assertEquals("existingFirstName", savedGamePlayer.getFirstName());
    assertEquals("existingLastName", savedGamePlayer.getLastName());
    assertNull("game player points should be null", savedGamePlayer.getTocPoints());
    assertEquals("game player finish should be 10", 10, savedGamePlayer.getPlace().intValue());
    assertTrue("game player knocked out should be true", savedGamePlayer.isKnockedOut());
    assertTrue("game player round updates should be true", savedGamePlayer.isRoundUpdates());
    assertTrue("game player buy-in collected should be true", savedGamePlayer.isBoughtIn());
    assertTrue("game player rebuy add on collected should be true", savedGamePlayer.isRebought());
    assertTrue("game player annual toc collected should be true", savedGamePlayer.isAnnualTocParticipant());
    assertTrue("game player quarterly toc collected should be true", savedGamePlayer.isQuarterlyTocParticipant());
    assertEquals("game player chop should be 1000", 1000, savedGamePlayer.getChop().intValue());
  }

  @Test
  public void testUpdateGamePlayerRetainKnockedOutStatus() {
    // Arrange
    GamePlayer existingGamePlayer = GamePlayer.builder()
      .id(11)
      .gameId(55)
      .knockedOut(false)
      .build();

    Game currentGame = Game.builder()
      .id(55)
      .numPlayers(1)
      .finalized(false)
      .players(Arrays.asList(existingGamePlayer))
      .build();

    when(gameHelper.get(55)).thenReturn(currentGame);

    doNothing().when(gameHelper).checkFinalized(any());

    // Same as game player
    GamePlayer gamePlayerToUpdate = GamePlayer.builder()
      .id(11)
      .gameId(55)
      .build();
    gamePlayerService.updateGamePlayer(gamePlayerToUpdate);

    ArgumentCaptor<Game> gameArg = ArgumentCaptor.forClass(Game.class);
    Mockito.verify(gameRepository).save(gameArg.capture());

    Game savedGame = gameArg.getValue();
    GamePlayer savedGamePlayer = savedGame.getPlayers().get(0);

    assertFalse("game player knocked out should still be false", savedGamePlayer.isKnockedOut());
  }

  @Test
  public void testKnockOut() {
    // Arrange
    GamePlayer existingGamePlayer = GamePlayer.builder()
      .id(11)
      .gameId(55)
      .knockedOut(false)
      .build();

    Game currentGame = Game.builder()
      .id(55)
      .numPlayers(1)
      .finalized(false)
      .players(Arrays.asList(existingGamePlayer))
      .build();

    when(gameHelper.get(55)).thenReturn(currentGame);

    doNothing().when(gameHelper).checkFinalized(any());

    gamePlayerService.toggleGamePlayerKnockedOut(55, 11);

    ArgumentCaptor<Game> gameArg = ArgumentCaptor.forClass(Game.class);
    Mockito.verify(gameRepository).save(gameArg.capture());

    Game savedGame = gameArg.getValue();
    GamePlayer savedGamePlayer = savedGame.getPlayers().get(0);
    assertTrue("game player knocked out should be true", savedGamePlayer.isKnockedOut());
  }

  @Test
  public void testKnockOutNotFound() {
    // Arrange
    GamePlayer someOtherGamePlayer = GamePlayer.builder()
      .id(999)
      .gameId(55)
      .knockedOut(false)
      .build();

    Game currentGame = Game.builder()
      .id(55)
      .numPlayers(1)
      .finalized(false)
      .players(Arrays.asList(someOtherGamePlayer))
      .build();

    when(gameHelper.get(55)).thenReturn(currentGame);

    doNothing().when(gameHelper).checkFinalized(any());

    assertThatThrownBy(() -> {
      gamePlayerService.toggleGamePlayerKnockedOut(55, 11);
    }).isInstanceOf(NotFoundException.class)
      .hasMessageContaining("Game player with id 11 not found");
  }

  @Test
  public void testUnknockOut() {
    // Arrange
    GamePlayer existingGamePlayer = GamePlayer.builder()
      .id(11)
      .gameId(55)
      .knockedOut(true)
      .build();

    Game currentGame = Game.builder()
      .id(55)
      .numPlayers(1)
      .finalized(false)
      .players(Arrays.asList(existingGamePlayer))
      .build();

    when(gameHelper.get(55)).thenReturn(currentGame);

    doNothing().when(gameHelper).checkFinalized(any());

    gamePlayerService.toggleGamePlayerKnockedOut(55, 11);

    ArgumentCaptor<Game> gameArg = ArgumentCaptor.forClass(Game.class);
    Mockito.verify(gameRepository).save(gameArg.capture());

    Game savedGame = gameArg.getValue();
    GamePlayer savedGamePlayer = savedGame.getPlayers().get(0);
    assertFalse("game player knocked out should be false", savedGamePlayer.isKnockedOut());
  }

  @Test
  public void testRebuy() {
    // Arrange
    GamePlayer existingGamePlayer = GamePlayer.builder()
      .id(11)
      .gameId(55)
      .rebought(false)
      .build();

    Game currentGame = Game.builder()
      .id(55)
      .numPlayers(1)
      .finalized(false)
      .players(Arrays.asList(existingGamePlayer))
      .build();

    when(gameHelper.get(55)).thenReturn(currentGame);

    doNothing().when(gameHelper).checkFinalized(any());

    gamePlayerService.toggleGamePlayerRebuy(55, 11);

    ArgumentCaptor<Game> gameArg = ArgumentCaptor.forClass(Game.class);
    Mockito.verify(gameRepository).save(gameArg.capture());

    Game savedGame = gameArg.getValue();
    GamePlayer savedGamePlayer = savedGame.getPlayers().get(0);
    assertTrue("game player rebuy should be true", savedGamePlayer.isRebought());
  }

  @Test
  public void testRebuyNotFound() {
    // Arrange
    GamePlayer someOtherGamePlayer = GamePlayer.builder()
      .id(99)
      .gameId(55)
      .rebought(false)
      .build();

    Game currentGame = Game.builder()
      .id(55)
      .numPlayers(1)
      .finalized(false)
      .players(Arrays.asList(someOtherGamePlayer))
      .build();

    when(gameHelper.get(55)).thenReturn(currentGame);

    doNothing().when(gameHelper).checkFinalized(any());

    assertThatThrownBy(() -> {
      gamePlayerService.toggleGamePlayerRebuy(55, 11);
    }).isInstanceOf(NotFoundException.class)
      .hasMessageContaining("Game player with id 11 not found");
  }

  @Test
  public void testUnrebuy() {
    // Arrange
    GamePlayer existingGamePlayer = GamePlayer.builder()
      .id(11)
      .gameId(55)
      .rebought(true)
      .build();

    Game currentGame = Game.builder()
      .id(55)
      .numPlayers(1)
      .finalized(false)
      .players(Arrays.asList(existingGamePlayer))
      .build();

    when(gameHelper.get(55)).thenReturn(currentGame);

    doNothing().when(gameHelper).checkFinalized(any());

    gamePlayerService.toggleGamePlayerRebuy(55, 11);

    ArgumentCaptor<Game> gameArg = ArgumentCaptor.forClass(Game.class);
    Mockito.verify(gameRepository).save(gameArg.capture());

    Game savedGame = gameArg.getValue();
    GamePlayer savedGamePlayer = savedGame.getPlayers().get(0);
    assertFalse("game player rebuy should be false", savedGamePlayer.isRebought());
  }

  @Test
  public void testDelete() {
    // Arrange
    GamePlayer existingGamePlayer = GamePlayer.builder()
      .id(11)
      .gameId(55)
      .rebought(true)
      .build();

    Game currentGame = Game.builder()
      .id(55)
      .numPlayers(1)
      .finalized(false)
      .players(Arrays.asList(existingGamePlayer))
      .build();

    when(gameHelper.get(55)).thenReturn(currentGame);

    doNothing().when(gameHelper).checkFinalized(any());

    gamePlayerService.deleteGamePlayer(55, 11);

    ArgumentCaptor<Game> gameArg = ArgumentCaptor.forClass(Game.class);
    Mockito.verify(gameRepository).save(gameArg.capture());

    Game savedGame = gameArg.getValue();
    assertEquals("game player should no longer be in list of game players", 0, savedGame.getPlayers().size());
  }

  public void assertNewlyCreatedGamePlayer(GamePlayer gamePlayer) {
    assertNull("game player points should be null", gamePlayer.getTocPoints());
    assertNull("game player finish should be null", gamePlayer.getPlace());
    assertFalse("game player knocked out should be false", gamePlayer.isKnockedOut());
    assertFalse("game player round updates should be false", gamePlayer.isRoundUpdates());
    assertFalse("game player buy-in collected should be false", gamePlayer.isBoughtIn());
    assertFalse("game player rebuy add on collected should be false", gamePlayer.isRebought());
    assertFalse("game player annual toc collected should be false", gamePlayer.isAnnualTocParticipant());
    assertFalse("game player quarterly toc collected should be false", gamePlayer.isQuarterlyTocParticipant());
    assertNull("game player chop should be null", gamePlayer.getChop());
  }

}
