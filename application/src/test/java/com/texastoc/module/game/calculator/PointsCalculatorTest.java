package com.texastoc.module.game.calculator;

import com.texastoc.TestConstants;
import com.texastoc.module.game.model.Game;
import com.texastoc.module.game.model.GamePlayer;
import com.texastoc.module.game.repository.GameRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PointsCalculatorTest implements TestConstants {

  private PointsCalculator pointsCalculator;
  private Random random = new Random(System.currentTimeMillis());

  private GameRepository gameRepository;

  @Before
  public void before() {
    gameRepository = mock(GameRepository.class);
    pointsCalculator = new PointsCalculator(CHOP_TENTH_PLACE_INCR, CHOP_TENTH_PLACE_POINTS, CHOP_MULTIPLIER, gameRepository);
  }

  @Test
  public void testNoPlayersNoPoints() {
    Game game = Game.builder()
      .id(1)
      .numPlayers(0)
      .players(Collections.emptyList())
      .build();

    pointsCalculator.calculate(game);
    verify(gameRepository, times(0)).save(any());
  }

  @Test
  public void test1PlayersNoPoints() {
    List<GamePlayer> gamePlayers = new ArrayList<>(1);
    gamePlayers.add(GamePlayer.builder()
      .build());

    Game game = Game.builder()
      .id(1)
      .numPlayers(1)
      .players(gamePlayers)
      .build();

    pointsCalculator.calculate(game);
    verify(gameRepository, times(0)).save(any());
  }

  /*
   * Of the seven players
   * 1st place gets toc and qtoc points and
   * 3rd place gets toc points and
   * 5th gets qtoc points
   */
  @Test
  public void test7Players() {
    int numPlayers = 7;
    List<GamePlayer> gamePlayers = new ArrayList<>(numPlayers);
    for (int i = 0; i < numPlayers; i++) {
      gamePlayers.add(GamePlayer.builder()
        .place(i + 1)
        .build());
    }

    // first place both toc and qtoc
    gamePlayers.get(0).setAnnualTocParticipant(true);
    gamePlayers.get(0).setQuarterlyTocParticipant(true);
    int expectedFirstPlacePoints = pointsCalculator.calculatePlacePoints(7).get(1);

    // third place toc
    gamePlayers.get(2).setAnnualTocParticipant(true);
    int expectedThirdPlacePoints = pointsCalculator.calculatePlacePoints(7).get(3);

    // fifth place qtoc
    gamePlayers.get(4).setQuarterlyTocParticipant(true);
    int expectedFifthPlacePoints = pointsCalculator.calculatePlacePoints(7).get(5);

    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .players(gamePlayers)
      .build();

    pointsCalculator.calculate(game);

    ArgumentCaptor<Game> argument = ArgumentCaptor.forClass(Game.class);
    Mockito.verify(gameRepository, Mockito.times(1)).save(argument.capture());
    Game gameCalculated = argument.getValue();

    List<GamePlayer> actualGamePlayers = gameCalculated.getPlayers();

    // 1st place
    Assert.assertEquals(expectedFirstPlacePoints, actualGamePlayers.get(0).getTocPoints().intValue());
    Assert.assertEquals(expectedFirstPlacePoints, actualGamePlayers.get(0).getQTocPoints().intValue());

    // 2nd place
    Assert.assertNull(actualGamePlayers.get(1).getTocPoints());
    Assert.assertNull(actualGamePlayers.get(1).getQTocPoints());

    // 3rd place
    Assert.assertEquals(expectedThirdPlacePoints, actualGamePlayers.get(2).getTocPoints().intValue());
    Assert.assertNull(actualGamePlayers.get(2).getQTocPoints());

    // 4th place
    Assert.assertNull(actualGamePlayers.get(3).getTocPoints());
    Assert.assertNull(actualGamePlayers.get(3).getQTocPoints());

    // 5th place
    Assert.assertNull(actualGamePlayers.get(4).getTocPoints());
    Assert.assertEquals(expectedFifthPlacePoints, actualGamePlayers.get(4).getQTocPoints().intValue());

    // 6th place
    Assert.assertNull(actualGamePlayers.get(5).getTocPoints());
    Assert.assertNull(actualGamePlayers.get(5).getQTocPoints());

    // 7th place
    Assert.assertNull(actualGamePlayers.get(6).getTocPoints());
    Assert.assertNull(actualGamePlayers.get(6).getQTocPoints());
  }

//  @Test
//  public void test10PlayersWith2Finished() {
//
//    int numPlayers = 10;
//
//    Game game = Game.builder()
//      .id(1)
//      .numPlayers(numPlayers)
//      .build();
//
//    List<GamePlayer> gamePlayers = new ArrayList<>(numPlayers);
//    for (int i = 0; i < numPlayers; i++) {
//      gamePlayers.add(GamePlayer.builder()
//        .build());
//    }
//    gamePlayers.get(1).setPlace(10);
//    gamePlayers.get(4).setPlace(9);
//
//    // Needed in the persistPoints method
//    Mockito.when(gamePlayerRepository.selectById(Mockito.anyInt()))
//      .thenReturn(GamePlayer.builder()
//        .points(1)
//        .build());
//
//    gamePlayers = pointsCalculator.calculate(game, gamePlayers);
//
//    Assert.assertNotNull("list of game players should not be null", gamePlayers);
//    Assert.assertEquals("list of game players should be size " + numPlayers, numPlayers, gamePlayers.size());
//
//    int pointsCount = 0;
//    for (GamePlayer gamePlayer : gamePlayers) {
//      if (gamePlayer.getPoints() != null) {
//        ++pointsCount;
//      }
//    }
//
//    int pointsChop = 0;
//    for (GamePlayer gamePlayer : gamePlayers) {
//      if (gamePlayer.getChop() != null) {
//        ++pointsChop;
//      }
//    }
//
//    Assert.assertEquals("2 players should have points", 2, pointsCount);
//    Assert.assertEquals("no players should have chop points", 0, pointsChop);
//
//    Assert.assertEquals("10th place should have 7 points", 7, (int) gamePlayers.get(1).getPoints());
//    Assert.assertEquals("9th place should have 9 points", 9, (int) gamePlayers.get(4).getPoints());
//  }
//
//  @Test
//  public void test17PlayersWith10Finished() {
//
//    int numPlayers = 17;
//
//    Game game = Game.builder()
//      .id(1)
//      .numPlayers(numPlayers)
//      .build();
//
//    List<GamePlayer> gamePlayers = new ArrayList<>(numPlayers);
//    for (int i = 0; i < numPlayers; i++) {
//      gamePlayers.add(GamePlayer.builder()
//        .build());
//    }
//    gamePlayers.get(0).setPlace(4);
//    gamePlayers.get(1).setPlace(6);
//    gamePlayers.get(2).setPlace(7);
//    gamePlayers.get(3).setPlace(10);
//    gamePlayers.get(5).setPlace(9);
//    gamePlayers.get(8).setPlace(8);
//    gamePlayers.get(13).setPlace(5);
//    gamePlayers.get(16).setPlace(3);
//    gamePlayers.get(7).setPlace(2);
//    gamePlayers.get(15).setPlace(1);
//
//    // Needed in the persistPoints method
//    Mockito.when(gamePlayerRepository.selectById(Mockito.anyInt()))
//      .thenReturn(GamePlayer.builder()
//        .points(1)
//        .build());
//
//    gamePlayers = pointsCalculator.calculate(game, gamePlayers);
//
//    Assert.assertNotNull("list of game players should not be null", gamePlayers);
//    Assert.assertEquals("list of game players should be size " + numPlayers, numPlayers, gamePlayers.size());
//
//    int pointsCount = 0;
//    for (GamePlayer gamePlayer : gamePlayers) {
//      if (gamePlayer.getPoints() != null) {
//        ++pointsCount;
//      }
//    }
//
//    int pointsChop = 0;
//    for (GamePlayer gamePlayer : gamePlayers) {
//      if (gamePlayer.getChop() != null) {
//        ++pointsChop;
//      }
//    }
//
//    Assert.assertEquals("ten players should have points", 10, pointsCount);
//    Assert.assertEquals("no players should have chop points", 0, pointsChop);
//
//
//    Assert.assertEquals("1st place should have 105 points", 105, (int) gamePlayers.get(15).getPoints());
//    Assert.assertEquals("2nd place should have 81 points", 81, (int) gamePlayers.get(7).getPoints());
//    Assert.assertEquals("3rh place should have 63 points", 63, (int) gamePlayers.get(16).getPoints());
//    Assert.assertEquals("4th place should have 49 points", 49, (int) gamePlayers.get(0).getPoints());
//    Assert.assertEquals("5th place should have 38 points", 38, (int) gamePlayers.get(13).getPoints());
//    Assert.assertEquals("6th place should have 29 points", 29, (int) gamePlayers.get(1).getPoints());
//    Assert.assertEquals("7th place should have 23 points", 23, (int) gamePlayers.get(2).getPoints());
//    Assert.assertEquals("8th place should have 18 points", 18, (int) gamePlayers.get(8).getPoints());
//    Assert.assertEquals("9th place should have 14 points", 14, (int) gamePlayers.get(5).getPoints());
//    Assert.assertEquals("10th place should have 11 points", 11, (int) gamePlayers.get(3).getPoints());
//  }
//
//  @Test
//  public void test17PlayersWith2WayChop() {
//
//    int numPlayers = 17;
//
//    Game game = Game.builder()
//      .id(1)
//      .numPlayers(numPlayers)
//      .build();
//
//    List<GamePlayer> gamePlayers = new ArrayList<>(numPlayers);
//    for (int i = 0; i < numPlayers; i++) {
//      gamePlayers.add(GamePlayer.builder()
//        .build());
//    }
//    gamePlayers.get(0).setPlace(4);
//    gamePlayers.get(1).setPlace(6);
//    gamePlayers.get(2).setPlace(7);
//    gamePlayers.get(3).setPlace(10);
//    gamePlayers.get(5).setPlace(9);
//    gamePlayers.get(8).setPlace(8);
//    gamePlayers.get(13).setPlace(5);
//    gamePlayers.get(16).setPlace(3);
//
//    gamePlayers.get(7).setPlace(2);
//    gamePlayers.get(7).setChop(30000);
//
//    gamePlayers.get(15).setPlace(1);
//    gamePlayers.get(15).setChop(60000);
//
//    // Needed in the persistPoints method
//    Mockito.when(gamePlayerRepository.selectById(Mockito.anyInt()))
//      .thenReturn(GamePlayer.builder()
//        .points(1)
//        .build());
//
//    gamePlayers = pointsCalculator.calculate(game, gamePlayers);
//
//    Assert.assertNotNull("list of game players should not be null", gamePlayers);
//    Assert.assertEquals("list of game players should be size " + numPlayers, numPlayers, gamePlayers.size());
//
//    int pointsCount = 0;
//    for (GamePlayer gamePlayer : gamePlayers) {
//      if (gamePlayer.getPoints() != null) {
//        ++pointsCount;
//      }
//    }
//
//    int pointsChop = 0;
//    for (GamePlayer gamePlayer : gamePlayers) {
//      if (gamePlayer.getChop() != null) {
//        ++pointsChop;
//      }
//    }
//
//    Assert.assertEquals("ten players should have points", 10, pointsCount);
//    Assert.assertEquals("2 players should have chop points", 2, pointsChop);
//
//    Assert.assertEquals("1st place should have 97 points", 97, (int) gamePlayers.get(15).getPoints());
//    Assert.assertEquals("2nd place should have 89 points", 89, (int) gamePlayers.get(7).getPoints());
//
//  }
//
//  @Test
//  public void test17PlayersWith3WayChop() {
//
//    int numPlayers = 17;
//
//    Game game = Game.builder()
//      .id(1)
//      .numPlayers(numPlayers)
//      .build();
//
//    List<GamePlayer> gamePlayers = new ArrayList<>(numPlayers);
//    for (int i = 0; i < numPlayers; i++) {
//      gamePlayers.add(GamePlayer.builder()
//        .build());
//    }
//    gamePlayers.get(0).setPlace(4);
//    gamePlayers.get(1).setPlace(6);
//    gamePlayers.get(2).setPlace(7);
//    gamePlayers.get(3).setPlace(10);
//    gamePlayers.get(5).setPlace(9);
//    gamePlayers.get(8).setPlace(8);
//    gamePlayers.get(13).setPlace(5);
//    gamePlayers.get(16).setPlace(3);
//
//    gamePlayers.get(16).setPlace(3);
//    gamePlayers.get(16).setChop(25000);
//
//    gamePlayers.get(7).setPlace(2);
//    gamePlayers.get(7).setChop(75000);
//
//    gamePlayers.get(15).setPlace(1);
//    gamePlayers.get(15).setChop(100000);
//
//    // Needed in the persistPoints method
//    Mockito.when(gamePlayerRepository.selectById(Mockito.anyInt()))
//      .thenReturn(GamePlayer.builder()
//        .points(1)
//        .build());
//
//    gamePlayers = pointsCalculator.calculate(game, gamePlayers);
//
//    Assert.assertNotNull("list of game players should not be null", gamePlayers);
//    Assert.assertEquals("list of game players should be size " + numPlayers, numPlayers, gamePlayers.size());
//
//    int pointsCount = 0;
//    for (GamePlayer gamePlayer : gamePlayers) {
//      if (gamePlayer.getPoints() != null) {
//        ++pointsCount;
//      }
//    }
//
//    int pointsChop = 0;
//    for (GamePlayer gamePlayer : gamePlayers) {
//      if (gamePlayer.getChop() != null) {
//        ++pointsChop;
//      }
//    }
//
//    Assert.assertEquals("ten players should have points", 10, pointsCount);
//    Assert.assertEquals("3 players should have chop points", 3, pointsChop);
//
//    Assert.assertEquals("1st place should have 94 points", 94, (int) gamePlayers.get(15).getPoints());
//    Assert.assertEquals("2nd place should have 85 points", 85, (int) gamePlayers.get(7).getPoints());
//    Assert.assertEquals("3rd place should have 70 points", 70, (int) gamePlayers.get(16).getPoints());
//
//  }

}
