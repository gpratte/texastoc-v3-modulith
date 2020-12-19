package com.texastoc.service.calculator;

import com.texastoc.TestConstants;
import com.texastoc.model.game.Game;
import com.texastoc.model.game.GamePlayer;
import com.texastoc.repository.GamePlayerRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@RunWith(SpringRunner.class)
public class PointsCalculatorTest implements TestConstants {

  private PointsCalculator pointsCalculator;
  private Random random = new Random(System.currentTimeMillis());

  @MockBean
  private GamePlayerRepository gamePlayerRepository;

  @Before
  public void before() {
    pointsCalculator = new PointsCalculator(CHOP_TENTH_PLACE_INCR, CHOP_TENTH_PLACE_POINTS, CHOP_MULTIPLIER, gamePlayerRepository);
  }

  @Test
  public void testNoPlayersNoPoints() {

    Game game = Game.builder()
      .id(1)
      .numPlayers(0)
      .build();

    List<GamePlayer> gamePlayers = pointsCalculator.calculate(game, Collections.EMPTY_LIST);

    Assert.assertNotNull("list of game players should not be null", gamePlayers);
    Assert.assertEquals("list of game players should be size 0", 0, gamePlayers.size());
  }

  @Test
  public void test1PlayersNoPoints() {

    Game game = Game.builder()
      .id(1)
      .numPlayers(1)
      .build();

    List<GamePlayer> gamePlayers = new ArrayList<>(1);
    gamePlayers.add(GamePlayer.builder()
      .build());

    gamePlayers = pointsCalculator.calculate(game, gamePlayers);

    Assert.assertNotNull("list of game players should not be null", gamePlayers);
    Assert.assertEquals("list of game players should be size 1", 1, gamePlayers.size());
    Assert.assertNull("game players points should be null", gamePlayers.get(0).getPoints());
    Assert.assertNull("game players chop should be null", gamePlayers.get(0).getChop());
  }

  @Test
  public void testUpTo7Players1WithPoints() {

    // Create between 1 and 7 players
    int numPlayers = 0;
    while (numPlayers == 0) {
      numPlayers = random.nextInt(8);
    }

    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .build();

    List<GamePlayer> gamePlayers = new ArrayList<>(numPlayers);
    for (int i = 0; i < numPlayers; i++) {
      gamePlayers.add(GamePlayer.builder()
        .build());
    }
    int pickOnePlayer = random.nextInt(numPlayers);
    gamePlayers.get(pickOnePlayer).setPlace(numPlayers);

    // Needed in the persistPoints method
    Mockito.when(gamePlayerRepository.selectById(Mockito.anyInt()))
      .thenReturn(GamePlayer.builder()
        .points(1)
        .build());


    gamePlayers = pointsCalculator.calculate(game, gamePlayers);

    Assert.assertNotNull("list of game players should not be null", gamePlayers);
    Assert.assertEquals("list of game players should be size " + numPlayers, numPlayers, gamePlayers.size());

    int pointsCount = 0;
    for (GamePlayer gamePlayer : gamePlayers) {
      if (gamePlayer.getPoints() != null) {
        ++pointsCount;
      }
    }

    int pointsChop = 0;
    for (GamePlayer gamePlayer : gamePlayers) {
      if (gamePlayer.getChop() != null) {
        ++pointsChop;
      }
    }

    Assert.assertEquals("only one player should have points", 1, pointsCount);
    Assert.assertEquals("no players should have chop points", 0, pointsChop);
  }

  @Test
  public void test10PlayersWith2Finished() {

    int numPlayers = 10;

    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .build();

    List<GamePlayer> gamePlayers = new ArrayList<>(numPlayers);
    for (int i = 0; i < numPlayers; i++) {
      gamePlayers.add(GamePlayer.builder()
        .build());
    }
    gamePlayers.get(1).setPlace(10);
    gamePlayers.get(4).setPlace(9);

    // Needed in the persistPoints method
    Mockito.when(gamePlayerRepository.selectById(Mockito.anyInt()))
      .thenReturn(GamePlayer.builder()
        .points(1)
        .build());

    gamePlayers = pointsCalculator.calculate(game, gamePlayers);

    Assert.assertNotNull("list of game players should not be null", gamePlayers);
    Assert.assertEquals("list of game players should be size " + numPlayers, numPlayers, gamePlayers.size());

    int pointsCount = 0;
    for (GamePlayer gamePlayer : gamePlayers) {
      if (gamePlayer.getPoints() != null) {
        ++pointsCount;
      }
    }

    int pointsChop = 0;
    for (GamePlayer gamePlayer : gamePlayers) {
      if (gamePlayer.getChop() != null) {
        ++pointsChop;
      }
    }

    Assert.assertEquals("2 players should have points", 2, pointsCount);
    Assert.assertEquals("no players should have chop points", 0, pointsChop);

    Assert.assertEquals("10th place should have 7 points", 7, (int) gamePlayers.get(1).getPoints());
    Assert.assertEquals("9th place should have 9 points", 9, (int) gamePlayers.get(4).getPoints());
  }

  @Test
  public void test17PlayersWith10Finished() {

    int numPlayers = 17;

    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .build();

    List<GamePlayer> gamePlayers = new ArrayList<>(numPlayers);
    for (int i = 0; i < numPlayers; i++) {
      gamePlayers.add(GamePlayer.builder()
        .build());
    }
    gamePlayers.get(0).setPlace(4);
    gamePlayers.get(1).setPlace(6);
    gamePlayers.get(2).setPlace(7);
    gamePlayers.get(3).setPlace(10);
    gamePlayers.get(5).setPlace(9);
    gamePlayers.get(8).setPlace(8);
    gamePlayers.get(13).setPlace(5);
    gamePlayers.get(16).setPlace(3);
    gamePlayers.get(7).setPlace(2);
    gamePlayers.get(15).setPlace(1);

    // Needed in the persistPoints method
    Mockito.when(gamePlayerRepository.selectById(Mockito.anyInt()))
      .thenReturn(GamePlayer.builder()
        .points(1)
        .build());

    gamePlayers = pointsCalculator.calculate(game, gamePlayers);

    Assert.assertNotNull("list of game players should not be null", gamePlayers);
    Assert.assertEquals("list of game players should be size " + numPlayers, numPlayers, gamePlayers.size());

    int pointsCount = 0;
    for (GamePlayer gamePlayer : gamePlayers) {
      if (gamePlayer.getPoints() != null) {
        ++pointsCount;
      }
    }

    int pointsChop = 0;
    for (GamePlayer gamePlayer : gamePlayers) {
      if (gamePlayer.getChop() != null) {
        ++pointsChop;
      }
    }

    Assert.assertEquals("ten players should have points", 10, pointsCount);
    Assert.assertEquals("no players should have chop points", 0, pointsChop);


    Assert.assertEquals("1st place should have 105 points", 105, (int) gamePlayers.get(15).getPoints());
    Assert.assertEquals("2nd place should have 81 points", 81, (int) gamePlayers.get(7).getPoints());
    Assert.assertEquals("3rh place should have 63 points", 63, (int) gamePlayers.get(16).getPoints());
    Assert.assertEquals("4th place should have 49 points", 49, (int) gamePlayers.get(0).getPoints());
    Assert.assertEquals("5th place should have 38 points", 38, (int) gamePlayers.get(13).getPoints());
    Assert.assertEquals("6th place should have 29 points", 29, (int) gamePlayers.get(1).getPoints());
    Assert.assertEquals("7th place should have 23 points", 23, (int) gamePlayers.get(2).getPoints());
    Assert.assertEquals("8th place should have 18 points", 18, (int) gamePlayers.get(8).getPoints());
    Assert.assertEquals("9th place should have 14 points", 14, (int) gamePlayers.get(5).getPoints());
    Assert.assertEquals("10th place should have 11 points", 11, (int) gamePlayers.get(3).getPoints());
  }

  @Test
  public void test17PlayersWith2WayChop() {

    int numPlayers = 17;

    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .build();

    List<GamePlayer> gamePlayers = new ArrayList<>(numPlayers);
    for (int i = 0; i < numPlayers; i++) {
      gamePlayers.add(GamePlayer.builder()
        .build());
    }
    gamePlayers.get(0).setPlace(4);
    gamePlayers.get(1).setPlace(6);
    gamePlayers.get(2).setPlace(7);
    gamePlayers.get(3).setPlace(10);
    gamePlayers.get(5).setPlace(9);
    gamePlayers.get(8).setPlace(8);
    gamePlayers.get(13).setPlace(5);
    gamePlayers.get(16).setPlace(3);

    gamePlayers.get(7).setPlace(2);
    gamePlayers.get(7).setChop(30000);

    gamePlayers.get(15).setPlace(1);
    gamePlayers.get(15).setChop(60000);

    // Needed in the persistPoints method
    Mockito.when(gamePlayerRepository.selectById(Mockito.anyInt()))
      .thenReturn(GamePlayer.builder()
        .points(1)
        .build());

    gamePlayers = pointsCalculator.calculate(game, gamePlayers);

    Assert.assertNotNull("list of game players should not be null", gamePlayers);
    Assert.assertEquals("list of game players should be size " + numPlayers, numPlayers, gamePlayers.size());

    int pointsCount = 0;
    for (GamePlayer gamePlayer : gamePlayers) {
      if (gamePlayer.getPoints() != null) {
        ++pointsCount;
      }
    }

    int pointsChop = 0;
    for (GamePlayer gamePlayer : gamePlayers) {
      if (gamePlayer.getChop() != null) {
        ++pointsChop;
      }
    }

    Assert.assertEquals("ten players should have points", 10, pointsCount);
    Assert.assertEquals("2 players should have chop points", 2, pointsChop);

    Assert.assertEquals("1st place should have 97 points", 97, (int) gamePlayers.get(15).getPoints());
    Assert.assertEquals("2nd place should have 89 points", 89, (int) gamePlayers.get(7).getPoints());

  }

  @Test
  public void test17PlayersWith3WayChop() {

    int numPlayers = 17;

    Game game = Game.builder()
      .id(1)
      .numPlayers(numPlayers)
      .build();

    List<GamePlayer> gamePlayers = new ArrayList<>(numPlayers);
    for (int i = 0; i < numPlayers; i++) {
      gamePlayers.add(GamePlayer.builder()
        .build());
    }
    gamePlayers.get(0).setPlace(4);
    gamePlayers.get(1).setPlace(6);
    gamePlayers.get(2).setPlace(7);
    gamePlayers.get(3).setPlace(10);
    gamePlayers.get(5).setPlace(9);
    gamePlayers.get(8).setPlace(8);
    gamePlayers.get(13).setPlace(5);
    gamePlayers.get(16).setPlace(3);

    gamePlayers.get(16).setPlace(3);
    gamePlayers.get(16).setChop(25000);

    gamePlayers.get(7).setPlace(2);
    gamePlayers.get(7).setChop(75000);

    gamePlayers.get(15).setPlace(1);
    gamePlayers.get(15).setChop(100000);

    // Needed in the persistPoints method
    Mockito.when(gamePlayerRepository.selectById(Mockito.anyInt()))
      .thenReturn(GamePlayer.builder()
        .points(1)
        .build());

    gamePlayers = pointsCalculator.calculate(game, gamePlayers);

    Assert.assertNotNull("list of game players should not be null", gamePlayers);
    Assert.assertEquals("list of game players should be size " + numPlayers, numPlayers, gamePlayers.size());

    int pointsCount = 0;
    for (GamePlayer gamePlayer : gamePlayers) {
      if (gamePlayer.getPoints() != null) {
        ++pointsCount;
      }
    }

    int pointsChop = 0;
    for (GamePlayer gamePlayer : gamePlayers) {
      if (gamePlayer.getChop() != null) {
        ++pointsChop;
      }
    }

    Assert.assertEquals("ten players should have points", 10, pointsCount);
    Assert.assertEquals("3 players should have chop points", 3, pointsChop);

    Assert.assertEquals("1st place should have 94 points", 94, (int) gamePlayers.get(15).getPoints());
    Assert.assertEquals("2nd place should have 85 points", 85, (int) gamePlayers.get(7).getPoints());
    Assert.assertEquals("3rd place should have 70 points", 70, (int) gamePlayers.get(16).getPoints());

  }

}
