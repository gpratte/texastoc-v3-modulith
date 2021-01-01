package com.texastoc.module.game.service;

import com.texastoc.exception.NotFoundException;
import com.texastoc.module.game.calculator.GameCalculator;
import com.texastoc.module.game.calculator.PayoutCalculator;
import com.texastoc.module.game.calculator.PointsCalculator;
import com.texastoc.module.game.connector.WebSocketConnector;
import com.texastoc.module.game.exception.GameIsFinalizedException;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.repository.GameRepository;
import com.texastoc.module.notification.connector.EmailConnector;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GameHelperTest {

  private GameHelper gameHelper;
  private GameRepository gameRepository;
  private GameCalculator gameCalculator;
  private PayoutCalculator payoutCalculator;
  private PointsCalculator pointsCalculator;
  private SeasonModule seasonModule;
  private EmailConnector emailConnector = mock(EmailConnector.class);
  private WebSocketConnector webSocketConnector = mock(WebSocketConnector.class);

  @Before
  public void init() {
    gameRepository = mock(GameRepository.class);
    gameCalculator = mock(GameCalculator.class);
    payoutCalculator = mock(PayoutCalculator.class);
    seasonModule = mock(SeasonModule.class);
    gameHelper = new GameHelper(gameRepository, gameCalculator, payoutCalculator, pointsCalculator, emailConnector, webSocketConnector);
  }

  @Test
  public void getGame() {
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
    Mockito.verify(gameRepository, Mockito.times(1)).findById(111);
    assertEquals(111, actual.getId());
    assertEquals(now, actual.getDate());
  }

  @Test
  public void getGameNotFound() {
    // Arrange
    when(gameRepository.findById(111)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> {
      gameHelper.get(111);
    }).isInstanceOf(NotFoundException.class)
      .hasMessageContaining("Game with id 111 not found");
  }

  @Test
  public void getCurrentUnfinalized() {
    // Arrange
    ReflectionTestUtils.setField(gameHelper, "seasonModule", seasonModule);
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
    Mockito.verify(gameRepository, Mockito.times(1)).findUnfinalizedBySeasonId(2);
    assertEquals(111, actual.getId());
    assertEquals(now, actual.getDate());
  }

  @Test
  public void getCurrentMostRecent() {
    // Arrange
    ReflectionTestUtils.setField(gameHelper, "seasonModule", seasonModule);
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
    Mockito.verify(gameRepository, Mockito.times(1)).findUnfinalizedBySeasonId(2);
    Mockito.verify(gameRepository, Mockito.times(1)).findMostRecentBySeasonId(2);
    assertEquals(112, actual.getId());
    assertEquals(now, actual.getDate());
  }

  @Test
  public void checkNotFinalized() {
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
  public void checkFinalized() {
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
}
