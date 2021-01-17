package com.texastoc.module.game.service;

import com.texastoc.exception.NotFoundException;
import com.texastoc.module.game.calculator.GameCalculator;
import com.texastoc.module.game.calculator.PayoutCalculator;
import com.texastoc.module.game.calculator.PointsCalculator;
import com.texastoc.module.game.connector.WebSocketConnector;
import com.texastoc.module.game.exception.GameIsFinalizedException;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.repository.GameRepository;
import com.texastoc.module.player.PlayerModule;
import com.texastoc.module.season.SeasonModule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GameHelperTest {

  private GameHelper gameHelper;
  private GameRepository gameRepository;
  private SeasonModule seasonModule;
  private WebSocketConnector webSocketConnector;
  private final GameCalculator gameCalculator = mock(GameCalculator.class);
  private final PayoutCalculator payoutCalculator = mock(PayoutCalculator.class);
  private final PointsCalculator pointsCalculator = mock(PointsCalculator.class);

  @Before
  public void init() {
    gameRepository = mock(GameRepository.class);
    PlayerModule playerModule = mock(PlayerModule.class);
    seasonModule = mock(SeasonModule.class);
    webSocketConnector = mock(WebSocketConnector.class);
    gameHelper = new GameHelper(gameRepository, gameCalculator, payoutCalculator, pointsCalculator, webSocketConnector);
    ReflectionTestUtils.setField(gameHelper, "playerModule", playerModule);
    ReflectionTestUtils.setField(gameHelper, "seasonModule", seasonModule);
  }

  @Test
  public void testGetGame() {
    // Arrange
    LocalDate now = LocalDate.now();
    Game game = Game.builder()
      .id(111)
      .date(now)
      .build();
    when(gameRepository.findById(111)).thenReturn(Optional.of(game));

    // Act
    Game actual = gameHelper.get(111);

    // Assert
    verify(gameRepository, Mockito.times(1)).findById(111);
    assertEquals(111, actual.getId());
    assertEquals(now, actual.getDate());
  }

  @Test
  public void testGetGameNotFound() {
    // Arrange
    when(gameRepository.findById(111)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> {
      gameHelper.get(111);
    }).isInstanceOf(NotFoundException.class)
      .hasMessageContaining("Game with id 111 not found");
  }

  @Test
  public void testGetCurrentUnfinalized() {
    // Arrange
    when(seasonModule.getCurrentSeasonId()).thenReturn(2);

    LocalDate now = LocalDate.now();
    Game game = Game.builder()
      .id(111)
      .date(now)
      .build();
    List<Game> games = new ArrayList<>();
    games.add(game);
    when(gameRepository.findUnfinalizedBySeasonId(2)).thenReturn(games);

    // Act
    Game actual = gameHelper.getCurrent();

    // Assert
    verify(gameRepository, Mockito.times(1)).findUnfinalizedBySeasonId(2);
    assertEquals(111, actual.getId());
    assertEquals(now, actual.getDate());
  }

  @Test
  public void testGetCurrentMostRecent() {
    // Arrange
    when(seasonModule.getCurrentSeasonId()).thenReturn(2);
    when(gameRepository.findUnfinalizedBySeasonId(2)).thenReturn(Collections.emptyList());

    LocalDate now = LocalDate.now();
    Game game = Game.builder()
      .id(112)
      .date(now)
      .build();
    List<Game> games = new ArrayList<>();
    games.add(game);
    when(gameRepository.findMostRecentBySeasonId(2)).thenReturn(games);

    // Act
    Game actual = gameHelper.getCurrent();

    // Assert
    verify(gameRepository, Mockito.times(1)).findUnfinalizedBySeasonId(2);
    verify(gameRepository, Mockito.times(1)).findMostRecentBySeasonId(2);
    assertEquals(112, actual.getId());
    assertEquals(now, actual.getDate());
  }

  @Test
  public void testCheckNotFinalized() {
    // Arrange
    LocalDate now = LocalDate.now();
    Game game = Game.builder()
      .finalized(false)
      .build();

    // Act
    // No exception should be thrown
    gameHelper.checkFinalized(game);
  }

  @Test
  public void testCheckFinalized() {
    // Arrange
    LocalDate now = LocalDate.now();
    Game game = Game.builder()
      .finalized(true)
      .build();

    // Act & Assert
    assertThatThrownBy(() -> {
      gameHelper.checkFinalized(game);
    }).isInstanceOf(GameIsFinalizedException.class)
      .hasMessageContaining("Game is finalized");
  }

  @Test
  public void testRecalculate() {
    // Arrange
    Game game = new Game();
    Game calculatgedGame = new Game();

    when(gameCalculator.calculate(game)).thenReturn(calculatgedGame);

    // Act
    gameHelper.recalculate(game.getId());

    // Assert
    verify(gameCalculator, Mockito.times(1)).calculate(game);
    verify(payoutCalculator, Mockito.times(1)).calculate(calculatgedGame);
    verify(pointsCalculator, Mockito.times(1)).calculate(calculatgedGame);
  }

  @Test
  public void testSendUpdate() throws InterruptedException {
    // Arrange (same as the getCurrent test above)
    when(seasonModule.getCurrentSeasonId()).thenReturn(2);

    LocalDate now = LocalDate.now();
    Game game = Game.builder()
      .id(111)
      .date(now)
      .build();
    List<Game> games = new ArrayList<>();
    games.add(game);
    when(gameRepository.findUnfinalizedBySeasonId(2)).thenReturn(games);

    // Act
    gameHelper.sendUpdatedGame();

    // Assert
    // Since this is a thread call, sleep for a half a second first
    Thread.sleep(500l);
    verify(webSocketConnector, Mockito.times(1)).sendGame(any(Game.class));
  }
}
